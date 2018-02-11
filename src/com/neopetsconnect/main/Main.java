package com.neopetsconnect.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpResponse;
import com.logger.main.TimeUnits;
import com.neopetsconnect.bot.Bot;
import com.neopetsconnect.dailies.DailyDoer;
import com.neopetsconnect.exceptions.FaerieQuestException;
import com.neopetsconnect.faeriequest.FaerieQuestSolver;
import com.neopetsconnect.itemdb.JellyneoItemDatabase;
import com.neopetsconnect.restock.Restocker;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;
import com.neopetsconnect.utils.captcha.CaptchaSolver;

public class Main implements ConfigProperties, Categories {

  private static int MAX_EXCEPTION = 4;
  private static int exception_count = 0;
  public static Session session;

  public static void main(String[] args) {
    try {
      JellyneoItemDatabase.init();

      ConfigProperties.updateStatus(Status.ON);

      Logger.out.disableCategories(STORE);

      HttpHelper.log = LOG_REQUESTS;
      CaptchaSolver.logImages = LOG_CAPTCHA_IMAGES;
      HttpHelper helper = initHelper();
      handleSession(helper);
      if (DEBUG_ENABLED) {
        helper.setRandomDelay(DELAY, DELAY);
      }

      ExecutorService execService = Executors.newCachedThreadPool();

      execService.submit(() -> {
        while (true) {
          try {
            new Bot().run();
          } catch (Throwable th) {
            Logger.out.log(EXCEPTION, Utils.stackTraceToString(th));
          }
        }
      });

      execService.submit(() -> {
        while (true) {
          try {
            try {
              new DailyDoer(helper).run();
            } catch (FaerieQuestException | RuntimeException e) {
              if (!(e instanceof FaerieQuestException)
                  && !(e.getCause() instanceof FaerieQuestException)) {
                throw e;
              }
              if (ConfigProperties.FAERIE_QUEST_ENABLED) {
                System.out.println("enabling");
                new FaerieQuestSolver(helper).solveQuest();
              } else {
                System.out.println("syso!!");
                throw new FaerieQuestException(e.getMessage());
              }
            }
          } catch (Throwable th) {
            System.out.println(th);
            if (!checkExceptionLimit()) {
              System.exit(1);
            }
            Logger.out.log(EXCEPTION, Utils.stackTraceToString(th));
          }
        }
      });

      Logger.out.log("Waiting for dailies to be finished.");
      Utils.sleepAndLog(DAILIES, DAILIES_WAIT_SLEEP, TimeUnits.MINUTES);

      if (ConfigProperties.RESTOCK_ENABLED) {
        execService.submit(() -> {
          while (true) {
            try {
              new Restocker(helper).restockLoop();
            } catch (Throwable th) {
              if (!checkExceptionLimit()) {
                System.exit(1);
              }
              Logger.out.log(EXCEPTION, Utils.stackTraceToString(th));
            }
          }
        });
      }
    } catch (Throwable th) {
      Logger.out.log(EXCEPTION, Utils.stackTraceToString(th));
      System.exit(1);
    }
  }

  private static boolean checkExceptionLimit() {
    exception_count++;
    Logger.out.log(EXCEPTION, "Count: " + exception_count);
    if (exception_count > MAX_EXCEPTION) {
      return false;
    }
    return true;
  }

  public static void handleSession(HttpHelper helper) {
    handleSession(helper, USERNAME, PASSWORD, MY_SHOP_USING_PIN ? PIN : null);
  }

  public static void handleSession(HttpHelper helper, String username, String password,
      String pin) {
    Main.session = new Session(helper, username, password, pin);

    String indexContent = index(helper).getContent().get();


    if (!indexContent.contains(">" + username + "<")) {
      if (indexContent.contains("Welcome")) {
        session.logout();
      }
      HttpResponse loginResp = session.login();
      Logger.out.log("SESSION",
          loginResp.getContent().get().contains(username) ? "Logged in!" : "Something went wrong");
    } else {
      Logger.out.log("SESSION", "Already logged :)");
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
