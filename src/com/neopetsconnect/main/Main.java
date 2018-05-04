package com.neopetsconnect.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpResponse;
import com.logger.main.TimeUnits;
import com.neopetsconnect.bot.Bot;
import com.neopetsconnect.dailies.DailyDoer;
import com.neopetsconnect.exceptions.FaerieQuestException;
import com.neopetsconnect.exceptions.LoggedOutException;
import com.neopetsconnect.exceptions.ShopWizardBannedException;
import com.neopetsconnect.faeriequest.FaerieQuestSolver;
import com.neopetsconnect.itemdb.JellyneoItemDatabase;
import com.neopetsconnect.restock.Restocker;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;
import com.neopetsconnect.utils.captcha.CaptchaSolver;

public class Main implements Categories {

  private static int MAX_EXCEPTION = 5;
  private static int exception_count = 0;
  public static Session session;
  
  private final ExecutorService execService;
  private final Bot bot;
  
  private Main(ExecutorService execService, Bot bot) {
    this.execService = execService;
    this.bot = bot;
  }

  public static void main(String[] args) {
    JellyneoItemDatabase.init();

    Logger.out.disableCategories(STORE);
    
    final Bot bot = new Bot();

    ExecutorService execService = Executors.newCachedThreadPool();
    
    execService.submit(() -> {
      while (!ConfigProperties.getStatus().equals(Status.OFF)) {          
        try {
          bot.run();
          while (!ConfigProperties.getStatus().equals(Status.OFF)) {
            Utils.sleep(0.01);
          }
        } catch (Throwable th) {
          Logger.out.log(EXCEPTION, Utils.stackTraceToString(th));
        }
        Logger.out.log(BOT, "Bot ended.");
      }
    });
    
    ConfigProperties.updateStatus(Status.ON);
    while (!ConfigProperties.getStatus().equals(Status.OFF)) { 
      if (ConfigProperties.getStatus().equals(Status.STOPPED)) {
        Utils.sleep(0.1);
        continue;
      }
      Logger.out.log(MAIN, "Running...");
      Main main = new Main(execService, bot);
      main.run();
      Logger.out.log(MAIN, "Stopped.");
    }
    
    bot.disconnect();
    
    execService.shutdown();
    System.exit(0);
  }
  
  private void run() {
    try {
      HttpHelper.log = ConfigProperties.isLogRequests();
      CaptchaSolver.logImages = ConfigProperties.isLogCaptchaImages();
      HttpHelper helper = initHelper();
      handleSession(helper);
      if (ConfigProperties.isDebugEnabled()) {
        helper.setRandomDelay(ConfigProperties.getDelay(), ConfigProperties.getDelay());
      }

      System.out.println("submitting dailies job.");
      execService.submit(() -> {
        while (!ConfigProperties.getStatus().isStoppedOrOff()) {
          try {
            try {
              System.out.println("new dailies");
              new DailyDoer(helper).run();
            } catch (LoggedOutException e) {
              System.out.println(e);
              session.login();
            } catch (FaerieQuestException | RuntimeException e) {
              System.out.println(e);
              if (e instanceof FaerieQuestException
                  || e.getCause() instanceof FaerieQuestException) {
                if (ConfigProperties.isFaerieQuestEnabled()) {
                  System.out.println("enabling");
                  new FaerieQuestSolver(helper).solveQuest();
                } else {
                  System.out.println("syso!!");
                  throw new FaerieQuestException(e.getMessage());
                }
              }
              if (e instanceof ShopWizardBannedException
                  || e.getCause() instanceof ShopWizardBannedException) {
                Logger.out.log(SHOP_WIZARD, "Shop wizard banned!");
                Utils.sleepAndLog(SHOP_WIZARD, 60 * 60);
              }
              throw e;
            }
          } catch (Throwable th) {
            System.out.println(th);
            Logger.out.log(EXCEPTION, Utils.stackTraceToString(th));
            if (!checkExceptionLimit()) {
              break;
            }
          }
        }
        bot.send("Dailies stopped!");
        Logger.out.log(DAILIES, "Dailies ended.");
      });

      Logger.out.log("Waiting for dailies to be finished.");
      Utils.sleepAndLog(DAILIES, ConfigProperties.getDailiesWaitSleep(), TimeUnits.MINUTES);

      System.out.println("submitting restock");
      execService.submit(() -> {
        while (!ConfigProperties.getStatus().isStoppedOrOff()) {
          try {
            System.out.println("new restock");
            new Restocker(helper).restockLoop();
          } catch (LoggedOutException e) {
            session.login();
          } catch (Throwable th) {
            Logger.out.log(EXCEPTION, Utils.stackTraceToString(th));
            if (!checkExceptionLimit()) {
                break;
            }
          }
        }
        bot.send("Restock stopped!");
        Logger.out.log(RESTOCK, "Restock ended.");
      });
    } catch (Throwable th) {
      Logger.out.log(EXCEPTION, Utils.stackTraceToString(th));
    }
    System.out.println("after submitting");
    while (!ConfigProperties.getStatus().isStoppedOrOff()) {
      Utils.sleep(0.1);
    }
    bot.send("Main stopped!");
    Logger.out.log(MAIN, "Main ended.");    
  }

  private boolean checkExceptionLimit() {
    exception_count++;
    Logger.out.log(EXCEPTION, "Count: " + exception_count);
    if (exception_count > MAX_EXCEPTION) {
      return false;
    }
    return true;
  }

  public static void handleSession(HttpHelper helper) {
    handleSession(helper, ConfigProperties.getUsername(), ConfigProperties.getPassword(),
        ConfigProperties.isMyShopUsingPin() ? ConfigProperties.getPin() : null);
  }

  public static void handleSession(HttpHelper helper, String username, String password,
      String pin) {
    Main.session = new Session(helper, username, password, pin);

    String indexContent = index(helper).getContent().get();

    if (!indexContent.contains("Welcome, <a href=\"/userlookup.phtml?user=" + username + "\">")) {
      if (indexContent.contains("Welcome")) {
    	Logger.out.log(SESSION, "Logging out.");
        session.logout();
      }
      HttpResponse loginResp = session.login();
      Logger.out.log(SESSION,
          loginResp.getContent().get().contains(username) ? "Logged in!" : "Something went wrong");
    } else {
      Logger.out.log(SESSION, "Already logged :)");
    }
  }

  public static HttpHelper initHelper() {
    HttpHelper helper = new HttpHelper("www.neopets.com").addDefaultHeader("Accept", "text/html")
        .addDefaultHeader("Accept-Language", "en;q=0.8")
        .addDefaultHeader("Cache-Control", "no-cache").addDefaultHeader("Connection", "keep-alive")
        .addDefaultHeader("Origin", "http://www.neopets.com").addDefaultHeader("Pragma", "no-cache")
        .addDefaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
    return helper;
  }

  public static HttpResponse index(HttpHelper helper) {
    return helper.get("/index.phtml").send();
  }
}
