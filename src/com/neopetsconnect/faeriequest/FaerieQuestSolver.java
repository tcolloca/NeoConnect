package com.neopetsconnect.faeriequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.exceptions.FaerieQuestBlockedException;
import com.neopetsconnect.inventory.Inventory;
import com.neopetsconnect.main.Item;
import com.neopetsconnect.main.Main;
import com.neopetsconnect.main.Status;
import com.neopetsconnect.myshop.MyShop;
import com.neopetsconnect.shopwizard.ShopItem;
import com.neopetsconnect.shopwizard.ShopWizard;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class FaerieQuestSolver implements Categories {

  private final static String CATEGORY = FAERIE_QUEST;
  private final HttpHelper helper;

  public FaerieQuestSolver(HttpHelper helper) {
    this.helper = helper;
  }

  public void solveQuest() throws FaerieQuestBlockedException {
    ConfigProperties.updateStatus(Status.PAUSED);
    try {
      Logger.out.log(CATEGORY, "Solving Faerie Quest");
      Optional<Item> item = talkToFaerie();
      if (!item.isPresent()) {
        return;
      }
      Logger.out.log(CATEGORY, "Item asked: " + item.get().getName());
      Main.handleSession(helper, ConfigProperties.getSideUsername(), 
          ConfigProperties.getSidePassword(), null);
      Logger.out.log(CATEGORY, "Switched to side");
      Map<Item, List<ShopItem>> bought = new ShopWizard(helper)
          .buyIfWorthIt(Arrays.asList(item.get()), ConfigProperties.getFaerieQuestMaxCost(),
              ConfigProperties.getShopWizSearchTimes());
      if (bought.isEmpty()) {
        Logger.out.log(CATEGORY, "Failed buying item");
        return;
      }
      Logger.out.log(CATEGORY, "Bought: " + bought.get(item.get()).get(0));
      new Inventory(helper).moveToShop(item.get());
      boolean updatePriceResult = new MyShop(helper).changePrice(item.get().getName(),
          bought.get(item.get()).get(0).getPrice().get(), 1);
      if (!updatePriceResult) {
        throw new IllegalStateException("Failed updating price of item");
      }
      Logger.out.log(CATEGORY, "Updated price on side shop");
      Logger.out.log("Next Username: "+ ConfigProperties.getUsername());
      Main.handleSession(helper, ConfigProperties.getUsername(), 
          ConfigProperties.getPassword(), ConfigProperties.isMyShopUsingPin() ? 
              ConfigProperties.getPin() : null);
      Logger.out.log(CATEGORY, "Switched to main");
      ShopItem sideBought = new ShopWizard(helper).buyFromUser(ConfigProperties.getSideUsername(),
          item.get());
      Logger.out.log(CATEGORY, "Bought from side: " + sideBought);
      Logger.out.log(CATEGORY, "Talking again to Faerie...");
      talkToFaerie();
      Utils.sleep(10);
    } finally {
      ConfigProperties.updateStatus(Status.ON);
    }
  }

  private Optional<Item> talkToFaerie() throws FaerieQuestBlockedException {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(faerieQuestPageRequest());
      Element questContent = content.getElementById("fq2");
      String faerie = questContent.getElementsByClass("description_top").first().text()
          .replaceAll("(You are currently on a quest for (a|an) |Faerie|\\.)", "").toLowerCase()
          .trim();
      List<String> toAccept = ConfigProperties.getFaeriesToAccept();
      List<String> toReject = ConfigProperties.getFaeriesToReject();
      Elements item = questContent.getElementsByClass("item");
      if (!item.isEmpty()) {
        String name = item.first().select(":root > b").first().text();
        String ck =
            content.getElementById("fq2_form").select(":root > input[name='ck']").first().val();
        if (toReject != null && toReject.contains(faerie)) {
          if (questContent.getElementById("abandon_faerie_quest") != null) {
        	Logger.out.log(CATEGORY, "Trying to abandon...");
            loadAbandonQuest(ck);
            Logger.out.log(CATEGORY, "Rejected quest from " + faerie + " faerie.");
            return Optional.empty();
          }
        }
        if (toAccept != null && !toAccept.contains("all") && !toAccept.contains(faerie)) {
          Logger.out.log(CATEGORY, "Ignored quest from " + faerie + " faerie.");
          return Optional.empty();
        } else if (questContent.getElementById("complete_faerie_quest") != null) {
          Logger.out.log(CATEGORY, "Trying to complete...");
          loadCompleteQuest(ck);
          Logger.out.log(CATEGORY, "Completed quest from " + faerie + " faerie.");
          return Optional.empty();
        }
        Logger.out.log(CATEGORY, "Maybe complete button not found?");
        return Optional.of(new Item(name));
      } else {
    	Logger.out.log(CATEGORY, "Item not found.");
        String description = questContent.getElementsByClass("description_bottom").first().text();
        throw new FaerieQuestBlockedException(description);
      }
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpResponse loadAbandonQuest(String ck) {
    return helper.post("/quests.phtml")
        .addHeader(Headers.referer("http://www.neopets.com/quests.phtml"))
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addFormParameter("action", "abandon").addFormParameter("ck", ck).send();
  }

  private HttpResponse loadCompleteQuest(String ck) {
    return helper.post("/quests.phtml")
        .addHeader(Headers.referer("http://www.neopets.com/quests.phtml"))
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addFormParameter("action", "complete").addFormParameter("ck", ck).send();
  }

  private HttpRequest faerieQuestPageRequest() {
    return helper.get("/quests.phtml")
        .addHeader(Headers.referer("http://www.neopets.com/market.phtml?type=wizard"));
  }
}
