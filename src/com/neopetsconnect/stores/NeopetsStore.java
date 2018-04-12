package com.neopetsconnect.stores;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.logger.main.TimeUnits;
import com.neopetsconnect.exceptions.NotEnoughNeopointsException;
import com.neopetsconnect.itemdb.ItemDatabase;
import com.neopetsconnect.main.Item;
import com.neopetsconnect.shopwizard.ShopItem;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;
import com.neopetsconnect.utils.captcha.CaptchaSolver;

public class NeopetsStore implements Categories {

  private static final String CATEGORY = STORE;
  private static final int UNKNOWN_PRICE = 151000;

  final HttpHelper helper;
  final int id;

  public NeopetsStore(HttpHelper helper, int id) {
    this.helper = helper;
    this.id = id;
  }

  public List<ShopItem> getWorthyItems(int minDiff) {
    return getWorthyItems(getItems(), minDiff);
  }

  public List<ShopItem> getWorthyItems(List<ShopItem> items, int minDiff) {
    ItemDatabase itemDb = ItemDatabase.getInstance();
    Logger.out.log(CATEGORY, items.size());
    return items.stream().collect(Collectors.toMap(item -> item, item -> {
      Item jellyItem = itemDb.getItem(item.getName());
      if (jellyItem != null && jellyItem.getPrice().isPresent()) {
        Logger.out.log(CATEGORY,
            item.getName() + " " + (jellyItem.forceGetPrice() - item.forceGetPrice()));
        return jellyItem.forceGetPrice() - item.forceGetPrice();
      }
      Logger.out.log(CATEGORY, item.getName() + " " + (UNKNOWN_PRICE - item.forceGetPrice()));
      return UNKNOWN_PRICE;
    })).entrySet().stream().filter(e -> e.getValue() >= minDiff)
        .sorted(Map.Entry.<ShopItem, Integer>comparingByValue().reversed()).map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  public List<ShopItem> getItems() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      List<ShopItem> items = new ArrayList<>();
      Element content = httpUtils.getContent(mainRequest());
      Element itemsForSale = content.select(":root > form[name=items_for_sale]").first();

      if (itemsForSale == null) {
        Logger.out.log(CATEGORY, "Sold out!");
        return items;
      }

      Elements itemElems = itemsForSale.select(":root > div > table > tbody > tr > td").first()
          .select(":root > div > table > tbody > tr").get(1)
          .select(":root > td > table > tbody > tr > td");

      itemElems.forEach(itemElem -> {
        String name = itemElem.select(":root > b").text();
        int price = Utils.getNps(itemElem.text().split("Cost:")[1]);
        String link = itemElem.select(":root > a").attr("href");
        items.add(new ShopItem(name, price, link, 1 /* TODO : Update available in NP store */));
      });

      return items;
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  public boolean buyItem(ShopItem item) throws NotEnoughNeopointsException {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Logger.out.logTimeStart("", TimeUnits.SECONDS);
      Document doc = httpUtils.getDocument(huggleRequest(item.getLink()));
      Logger.out.logTimeEnd("", CATEGORY, "Load huggle link: %.3f");
      Logger.out.logTimeStart("", TimeUnits.SECONDS);
      Logger.out.logTimeEnd("", CATEGORY, "Parse huggle link: %.3f");

      int nps = Integer.parseInt(doc.getElementById("npanchor").text().replaceAll(",", ""));

      if (nps < item.forceGetPrice()) {
        throw new NotEnoughNeopointsException(
            "Not enough neopoints. Expected: " + item.forceGetPrice() + " has: " + nps);
      }

      Element img = HttpUtils.getContent(doc).select(":root form > center > div > input").first();
      if (img == null) {
        Logger.out.log(RESTOCK, "Too late (?. Someone bought already: " + item.getName());
        return false;
      }
      String imgUrl = img.attr("src");

      Logger.out.logTimeStart("", TimeUnits.SECONDS);
      int[] point = CaptchaSolver.solve(helper, imgUrl);
      Logger.out.logTimeEnd("", CATEGORY, "Solve Captcha: %.3f");
      return parseBuyItemRequest(item.getLink(), item.forceGetPrice(), point);
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private boolean parseBuyItemRequest(String huggleLink, int offer, int[] point) {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(buyItemRequest(huggleLink, offer, point));
      return content.select(":root font > b").first().text()
          .startsWith("The Shopkeeper says 'I accept your offer");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpRequest buyItemRequest(String huggleLink, int offer, int[] point) {
    return helper.post("/haggle.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com" + huggleLink))
        .addFormParameter("current_offer", String.valueOf(offer))
        .addFormParameter("x", String.valueOf(point[0]))
        .addFormParameter("y", String.valueOf(point[1]));
  }

  private HttpRequest huggleRequest(String huggleLink) {
    return helper.get(huggleLink).addHeader(
        Headers.referer("http://www.neopets.com/objects.phtml?type=shop&obj_type=" + id));
  }

  public HttpRequest mainRequest() {
    return helper.get("/objects.phtml").addQueryParameter("type", "shop")
        .addQueryParameter("obj_type", String.valueOf(id));
  }
}
