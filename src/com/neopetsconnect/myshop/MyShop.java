package com.neopetsconnect.myshop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.exceptions.FaerieQuestException;
import com.neopetsconnect.exceptions.ItemNotFoundException;
import com.neopetsconnect.exceptions.ShopWizardBannedException;
import com.neopetsconnect.itemdb.JellyneoItemDatabase;
import com.neopetsconnect.main.Main;
import com.neopetsconnect.shopwizard.ShopWizard;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class MyShop implements Categories {

  public static final String CATEGORY = "MY_SHOP";

  final HttpHelper helper;

  public static void main(String[] args) throws FaerieQuestException {
    HttpHelper helper = Main.initHelper();
    Main.handleSession(helper, ConfigProperties.getSideUsername(), 
        ConfigProperties.getSidePassword(), null);

    MyShop shop = new MyShop(helper);
    shop.changePrice("Spyder", 799, 1);
    // shop.updatePrices(5, true, false, 0, 0.95);
  }

  public MyShop(HttpHelper helper) {
    this.helper = helper;
  }

  public Map<String, List<MyShopItem>> getItems() {
    return IntStream.range(0, getPageCount()).mapToObj(i -> getItemsFromPage(i))
        .flatMap(m -> m.entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  // Has to be a list because of secret lab maps.
  private Map<String, List<MyShopItem>> getItemsFromPage(int index) {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Map<String, List<MyShopItem>> items = new HashMap<>();
      Element content = httpUtils.getContent(pageRequest(index));
      Element form = content.select(":root > form[action=process_market.phtml]").first();
      if (form == null) {
        System.out.println("no form");
        return items;
      }
      Elements tableRows = form.select(":root > table > tbody > tr");
      int last = Main.session.usingPin() ? tableRows.size() - 2 : tableRows.size() - 1;
      List<Element> itemElems = tableRows.subList(1, last);

      itemElems.forEach(itemElem -> {
        Element idInfo = itemElem.select(":root > input[type=hidden]").first();
        String id = idInfo.attr("value");
        String rowId = idInfo.attr("name").split("obj_id_")[1];
        Elements cols = itemElem.select(":root > td");
        String name = cols.get(0).text();
        int oldCost = Integer.parseInt(cols.get(4).select(":root > input").attr("value"));
        items.putIfAbsent(name, new ArrayList<>());
        items.get(name).add(new MyShopItem(id, rowId, name, oldCost, index));
      });
      return items;
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  public void updatePrices(int iterations, boolean ignorePriced, boolean useJellyneo,
      double secsDelay, double percent) throws FaerieQuestException {
    // TODO: Update type of flag
    if (!ConfigProperties.isOrganizeInventoryEnabled()) {
      return;
    }
    for (int j = 0; j < getPageCount(); j++) {
      Map<String, List<MyShopItem>> items = getItemsFromPage(j);
      Set<String> itemNames = items.keySet();
      ShopWizard wiz = new ShopWizard(helper);
      int i = 0;
      for (String name : itemNames) {
        i++;
        MyShopItem item = items.get(name).get(0);
        if (ignorePriced && item.getPrice().get() != 0) {
          continue;
        }
        try {
          int price;
          if (useJellyneo) {
            price = JellyneoItemDatabase.getInstance().findPrice(item.getName());
            if ((int) (price * percent) > 999999) {
              price = 0;
            }
          } else {
            price = wiz.findPrice(item.getName(), iterations);
          }
          item.changePrice(price, percent);
          Logger.out.log(CATEGORY,
              String.format("Updated price: %s (%d/%d)", item.getName(), i, itemNames.size()));
        } catch (ItemNotFoundException e) {
          Logger.out.log(CATEGORY, "Price not found for: " + item.getName());
        } catch (ShopWizardBannedException e) {
          Logger.out.log(CATEGORY, "Shop wizard banned: " + e.getMessage());
          break;
        }
        Utils.sleep(secsDelay);
      }
      saveItems(Utils.merge(items.values()));
    }
  }

  public boolean changePrice(String name, int price, double percent) {
    for (int i = 0; i < getPageCount(); i++) {
      Map<String, List<MyShopItem>> items = getItemsFromPage(i);
      if (items.containsKey(name)) {
        for (MyShopItem item : items.get(name)) {
          item.changePrice(price, percent);
        }
        saveItemsInPage(Utils.merge(items.values()), i);
        return true;
      }
    }
    return false;
  }

  public void saveItem(MyShopItem item) {
    saveItems(Arrays.asList(item));
  }

  public void saveItems(Collection<MyShopItem> collection) {
    collection.stream().collect(Collectors.groupingBy(MyShopItem::getPageIndex)).entrySet().stream()
        .forEach(entry -> {
          Map<String, List<MyShopItem>> items = getItemsFromPage(entry.getKey());
          for (MyShopItem newItem : entry.getValue()) {
            List<MyShopItem> list = items.get(newItem.getName());
            MyShopItem oldItem = list.stream()
                .filter(it -> it.getRowId().equals(newItem.getRowId())).findFirst().get();
            list.remove(oldItem);
            list.add(newItem);
            items.put(newItem.getName(), list);
          }
          saveItemsInPage(Utils.merge(items.values()), entry.getKey());
        });
  }

  private void saveItemsInPage(Collection<MyShopItem> items, int index) {
    HttpRequest req = helper.post("/process_market.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(
            Headers.referer("http://www.neopets.com/market.phtml?type=your&view=&obj_name=&lim="
                + (index + 1) * 30 + "&order_by=id"))
        .addFormParameter("type", "update_prices").addFormParameter("order_by", "id")
        .addFormParameter("view", "").addFormParameter("lim", 30 * (index + 1))
        .addFormParameter("obj_name", "");

    if (Main.session.usingPin()) {
      req.addFormParameter("pin", Main.session.getPin());
    }

    for (MyShopItem item : items) {
      String rowId = item.getRowId();
      req = req.addFormParameter("obj_id_" + rowId, item.getId())
          .addFormParameter("oldcost_" + rowId, item.getOldCost())
          .addFormParameter("cost_" + rowId, item.getPrice().get())
          .addFormParameter("back_to_inv[" + item.getId() + "]", 0);
    }

    req.send();
  }

  public boolean takeAllMoney() {
    int money = getMoneyInTill();
    return takeMoney(money);
  }

  public boolean takeMoney(int nps) {
    loadShopTill();
    throw new UnsupportedOperationException();
  }

  private int getPageCount() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(pageRequest(0));
      if (content.select(":root > form[action=market.phtml]").first() == null) {
        return 1;
      }
      return (int) Math.ceil(Integer
          .valueOf(content.select(":root > form[action=market.phtml]").first().text().split("of")[1]
              .trim().replace("  ", ""))
          / 30.0);
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private int getMoneyInTill() {
    throw new UnsupportedOperationException();
  }

  private HttpRequest pageRequest(int index) {
    return helper.get("/market.phtml").addQueryParameter("type", "your")
        .addQueryParameter("view", "").addQueryParameter("obj_name", "")
        .addQueryParameter("lim", 30 * (index + 1)).addQueryParameter("order_by", "id");
  }

  private HttpResponse loadShopTill() {
    throw new UnsupportedOperationException();
  }
}
