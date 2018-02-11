package com.neopetsconnect.dailies;

import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.main.Item;
import com.neopetsconnect.main.Main;
import com.neopetsconnect.shopwizard.ShopWizard;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;

public class KitchenQuest implements ConfigProperties {

  private final static String CATEGORY = KITCHEN_QUEST;
  private final HttpHelper helper;
  private final ShopWizard shopWizard;

  public KitchenQuest(HttpHelper helper) {
    this.helper = helper;
    this.shopWizard = new ShopWizard(helper);
  }

  public static void main(String[] args) {
    HttpHelper helper = Main.initHelper();
    Main.handleSession(helper);

    KitchenQuest daily = new KitchenQuest(helper);
    int cost = daily.completeQuest(2000, 5);
    Logger.out.log(CATEGORY, cost);
  }

  public int call() {
    int maxSpend = KITCHEN_QUEST_DAILY_MAX_SPEND;
    int maxCost = KITCHEN_QUEST_MAX_COST;
    int questsDone = DailyLog.props.getInt(QUESTS, "quests_done", 0);
    int spent = DailyLog.props.getInt(QUESTS, "spent", 0);
    if (spent <= maxSpend && questsDone < 10) {
      int wasted = completeQuest(maxCost, SHOP_WIZ_SEARCH_TIMES);
      spent += wasted;
      if (wasted > 0) {
        DailyLog.props.addInt(QUESTS, "spent", spent);
        DailyLog.props.addInt(QUESTS, "quests_done", questsDone + 1);
        return 5;
      }
    }
    Logger.out.log(CATEGORY, "Done.");
    return DAILIES_REFRESH_FREQ / 2;
  }

  private int completeQuest(int maxCost, int searchTimes) {
    List<Item> items = getQuestItems();
    Logger.out.log(CATEGORY, "Items: "
        + String.join(", ", items.stream().map(it -> it.getName()).collect(Collectors.toList())));
    int wasted = shopWizard.buyIfWorthIt(items, maxCost, searchTimes).entrySet().stream()
        .mapToInt(entry -> entry.getValue().get(0).forceGetPrice()).sum();
    if (wasted > 0) {
      return giveItems() ? wasted : 0;
    }
    return wasted;
  }

  private List<Item> getQuestItems() {
    return getQuestItems(true);
  }

  private List<Item> getQuestItems(boolean reload) {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(mainRequest());
      Elements centers = content.select(":root > center");
      List<Element> itemElems;
      if (centers.size() < 2) {
        Elements rows = content.select(":root > form > table > tbody > tr");
        if (reload && rows.isEmpty()) {
          backToMysteryIsland();
          return getQuestItems(false);
        }
        itemElems = content.select(":root > form > table > tbody > tr").get(2).select(":root > td")
            .get(1).select(":root > b");
      } else {
        String dishName = content.select(":root > center").get(1).text();
        Logger.out.log(CATEGORY, "Dish name: " + dishName);
        content = httpUtils.getContent(askQuestRequest(dishName));
        itemElems = content.select(":root > table > tbody > tr > td > b");
      }
      return itemElems.stream().map(itemElem -> {
        String itemName = itemElem.text();
        return new Item(itemName);
      }).collect(Collectors.toList());
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private boolean giveItems() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      mainRequest().send();
      Element content = httpUtils.getContent(giveItemsRequest());
      return content.select(":root > center").get(0).select(":root > p > b").text()
          .contains("successfully");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpRequest mainRequest() {
    return helper.get("/island/kitchen.phtml");
  }

  private HttpRequest askQuestRequest(String name) {
    return helper.post("/island/kitchen2.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/island/kitchen.phtml"))
        .addFormParameter("food_desc", name);
  }

  private HttpRequest giveItemsRequest() {
    return helper.post("/island/kitchen2.phtml").addFormParameter("type", "gotingredients")
        .addHeader(Headers.referer("http://www.neopets.com/island/kitchen.phtml"))
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED));
  }

  private HttpResponse backToMysteryIsland() {
    return helper.get("/island/index.phtml").send();
  }
}
