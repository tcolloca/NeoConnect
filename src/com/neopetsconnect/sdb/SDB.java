package com.neopetsconnect.sdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.neopetsconnect.exceptions.FaerieQuestException;
import com.neopetsconnect.exceptions.ItemNotFoundException;
import com.neopetsconnect.exceptions.ShopWizardBannedException;
import com.neopetsconnect.main.Main;
import com.neopetsconnect.shopwizard.ShopWizard;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class SDB {

  public static final String CATEGORY = "SDB";

  final HttpHelper helper;

  public static void main(String[] args) {
    HttpHelper.log = true;
    HttpHelper.logPath = "logs";
    HttpHelper helper = Main.initHelper();
    helper.setRandomDelay(7, 20);
    Main.handleSession(helper);

    SDB shop = new SDB(helper);
    List<String> blacklist = Arrays.asList(
        "codestone",
        "scorchstone",
        "harris",
        "leaf shield",
        "magic hat",
        "golden compass",
        "scroll of knowledge",
        "trick-or-treat",
        "diadem of the deep",
        "scroll of ultranova",
        "abandoned water tower",
        "maraquan yo-yo",
        "wraith transformation",
        "spaceship interior background",
        "fiery golden collectable scarab",
        "enchanted map of",
        "biggest mistakes of my life",
        "fyora collectable charm",
        "malum coll",
        "recovered",
        "basic gift box",
        "Birthday cake slice",
        "golden chain bracelet"
        
        );
    shop.discardBetween(1, 1000, blacklist);
  }

  public SDB(HttpHelper helper) {
    this.helper = helper;
  }

  public void discardUnder(int price) {
    discardBetween(0, price, new ArrayList<>());
  }

  public void discardBetween(int minPrice, int maxPrice, List<String> blacklist) {
    ShopWizard wiz = new ShopWizard(helper);
//    JellyneoItemDatabase itemDb = JellyneoItemDatabase.getInstance();
    for (int index = getPageCount() - 1; index >= 0; index--) {
      List<SdbItem> items = getItemsInPage(index);
      List<SdbItem> toRemove = new ArrayList<>();
      for (SdbItem item : items) {
        boolean blacklisted = false;
        for (String blacklistedStr: blacklist) {
          if (item.getName().toLowerCase().contains(blacklistedStr.toLowerCase())) {
            Logger.out.log("Ignoring " + item.getName() + ". Cause: " + blacklistedStr);
            blacklisted = true;
            break;
          }
        }
        if (blacklisted) {
          continue;
        }
        int price ;
        try {
          try {
            price = wiz.findPrice(item.getName(), 2);
          } catch (ShopWizardBannedException e) {
            Utils.sleep(60 * 60);
            price = wiz.findPrice(item.getName(), 2);
//            throw new RuntimeException(e);
          }
          Logger.out.log(CATEGORY, item.getName() + " " + price);
          if (minPrice < price && price < maxPrice) {
            toRemove.add(item);
          }
        } catch (ItemNotFoundException e) {
          Logger.out.log(CATEGORY, "Item not found: " + item.getName());
        } catch (ShopWizardBannedException | FaerieQuestException e) {
          throw new RuntimeException(e);
        }
      }
      remove(toRemove, index);
//      new Inventory(helper).moveToShop(toRemove.stream().map(sdbItem -> new Item(sdbItem.getName()))
//          .collect(Collectors.toList()));
    }
  }

  private int getPageCount() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(sdbPageRequest(0));
      return content.select(":root > form > table").get(0).select(":root > tbody > tr").first()
          .select(":root > td").get(1).select(":root > form > select > option").size();
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private List<SdbItem> getItemsInPage(int index) {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(sdbPageRequest(index));
      if (content.text().contains("End of results")) {
        return new ArrayList<>();
      }
      List<Element> itemElems =
          content.select(":root > form > table").get(1).select(":root > tbody > tr").stream()
              .filter(elem -> elem.attr("bgcolor").startsWith("#F")).collect(Collectors.toList());
      List<SdbItem> items = new ArrayList<>();
      itemElems.forEach(itemElem -> {
        Elements cols = itemElem.select(":root > td");
        if (cols.attr("colspan").isEmpty()) {
          String name = cols.get(1).select(":root > b").first().ownText();
          String quantity = cols.get(4).text();
          String id = cols.get(5).select(":root > input").first().attr("name");
          items.add(new SdbItem(name, id, quantity));
        }
      });
      return items;
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private void remove(List<SdbItem> items, int index) {
    Logger.out.log(CATEGORY, "remove: " + items);

    HttpRequest req = helper.post("/process_safetydeposit.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer(
            "http://www.neopets.com/market.phtml?category=0&obj_name=&offset=" + (index * 30)))
        .addQueryParameter("checksub", "scan").addFormParameter("offset", index * 30)
        .addFormParameter("category", 0).addFormParameter("obj_name", "");

    for (SdbItem item : items) {
      req.addFormParameter(item.getId(), item.getQuantity());
    }

    req.send();
  }

  private HttpRequest sdbPageRequest(int index) {
    return helper.get("/safetydeposit.phtml").addQueryParameter("offset", 30 * index)
        .addQueryParameter("obj_name", "").addQueryParameter("category", 0);
  }
}
