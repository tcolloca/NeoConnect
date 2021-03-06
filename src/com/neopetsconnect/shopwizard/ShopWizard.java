package com.neopetsconnect.shopwizard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
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
import com.neopetsconnect.main.Item;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class ShopWizard implements Categories {

  private final static String CATEGORY = SHOP_WIZARD;
  private final HttpHelper helper;

  public ShopWizard(HttpHelper helper) {
    this.helper = helper;
  }

  public Map<Item, List<ShopItem>> buyIfWorthIt(List<Item> items, int maxCost, int times) {
    Map<Item, List<ShopItem>> shopItems = findItems(items, times);

    int totalCost = shopItems.entrySet().stream()
        .mapToInt(entry -> entry.getValue().get(0).forceGetPrice() * entry.getKey().getAmount())
        .sum();
    Logger.out.log(CATEGORY, "Total cost: %d, max cost: %d.", totalCost, maxCost);
    if (totalCost > maxCost) {
      Logger.out.log(CATEGORY, "Too expensive :/");
      return new HashMap<>();
    }

    return buyShopItems(shopItems);
  }

  public Map<Item, List<ShopItem>> buyItems(List<Item> items, int times) throws FaerieQuestException, ShopWizardBannedException {
	validateShopWizard();
    return buyShopItems(findItems(items, times));
  }

  private Map<Item, List<ShopItem>> buyShopItems(Map<Item, List<ShopItem>> items) {
    return items.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
      try {
        Logger.out.log(CATEGORY, "Buying: " + entry.getKey());
        return buyItem(entry.getValue(), entry.getKey().getAmount());
      } catch (ItemNotFoundException e) {
        throw new RuntimeException("Failed buying item.", e);
      }
    }));
  }

  public List<ShopItem> buyItem(List<ShopItem> shopItems, int amount) throws ItemNotFoundException {
    List<ShopItem> bought = new ArrayList<>();
    for (int i = 0; i < shopItems.size() && i < 3 * amount; i++) {
      for (int j = 0; j < shopItems.get(i).getAvailable() && bought.size() < amount; j++) {
        Logger.out.log(CATEGORY, "Buying item: " + shopItems.get(i));
        if (buyItem(shopItems.get(i))) {
          bought.add(shopItems.get(i));
        } else {
          break;
        }
      }
    }
    if (bought.size() < amount) {
      throw new ItemNotFoundException("Couldn't buy all of " + shopItems.get(0).getName());
    }
    return bought;
  }

  public boolean buyItem(ShopItem shopItem) {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(shopPageRequest(shopItem));
      if (content.text().contains("owner of this shop has been frozen")) {
        return false;
      }
      try {
        Elements links =
            content.select(":root > div").get(3).select(":root > table > tbody > tr > td > a");
        if (links.isEmpty()) {
          return false;
        }
        String link = links.get(0).attr("href");
        loadBuyPage(shopItem, link);
        return true; // TODO
      } catch (Exception e) {
        throw new RuntimeException("Failed when buying: " + shopItem.getName(), e);
      }
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  public int findPrice(String name, int iterations)
      throws ItemNotFoundException, ShopWizardBannedException, FaerieQuestException {
    validateShopWizard();
    return findItem(name, iterations).get(0).forceGetPrice();
  }

  public int findPrice(String name)
      throws ItemNotFoundException, ShopWizardBannedException, FaerieQuestException {
    validateShopWizard();
    return findPrice(name, 1);
  }

  private List<ShopItem> findItem(String name, int iterations)
      throws ItemNotFoundException, ShopWizardBannedException, FaerieQuestException {
    List<ShopItem> items = new ArrayList<>();
    for (int i = 0; i < iterations; i++) {
      try {
        items.addAll(findItem(name));
      } catch (ItemNotFoundException e) {
      }
    }
    if (items.isEmpty()) {
      throw new ItemNotFoundException(name + " not found.");
    }
    return items.stream().sorted(Comparator.comparing(ShopItem::forceGetPrice))
        .collect(Collectors.toList());
  }

  private Map<Item, List<ShopItem>> findItems(List<Item> items, int searchTimes) {
    return items.stream().collect(Collectors.toMap(item -> item, item -> {
      try {
        Logger.out.log(CATEGORY, "Finding: " + item.getName());
        return findItem(item.getName(), searchTimes);
      } catch (ItemNotFoundException | ShopWizardBannedException | FaerieQuestException e) {
        Logger.out.log(CATEGORY, "Failed finding item: " + item.getName());
        throw new RuntimeException(e);
      }
    }));
  }

  private List<ShopItem> findItem(String name)
      throws ItemNotFoundException, ShopWizardBannedException, FaerieQuestException {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(searchRequest(name));
      Element shopWizContent = content.select(":root > div").get(1);
      Elements tables = shopWizContent.select(":root > table");
      if (!tables.isEmpty()) {
        if (tables.size() == 1) {
          throw new ItemNotFoundException(name);
        }
        List<Element> itemElems =
            tables.get(1).select(":root > tbody > tr").stream().collect(Collectors.toList());
        itemElems.remove(0);
        return itemElems.stream().map(itemElem -> {
          int price = Utils.getNps(itemElem.child(3).text());
          String link = itemElem.child(0).child(0).attr("href");
          int available = Integer.parseInt(itemElem.child(2).text());
          return new ShopItem(name, price, link, available);
        }).collect(Collectors.toList());
      } else {
        String text = shopWizContent.select(":root > center > p").first().text();
        throw new ShopWizardBannedException(text);
      }
    } catch (Throwable th) {
      if (!(th instanceof ItemNotFoundException)) {
        httpUtils.logRequestResponse();
      }
      throw th;
    }
  }

  private void validateShopWizard() throws FaerieQuestException, ShopWizardBannedException {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(shopWizardRequest());
      Element shopWizContent = content.select(":root > div").get(1);
      Elements tables = shopWizContent.select(":root > table");
      if (tables.isEmpty()) {
        Elements centers = shopWizContent.select(":root > center");
        if (centers.isEmpty()) {
          Elements ps = shopWizContent.select(":root > p");
          if (!ps.isEmpty()) {
            String text = ps.first().text();
            throw new FaerieQuestException(text);
          }
        } else {
          String text = centers.select(":root > p").first().text();
          throw new ShopWizardBannedException(text);
        }
      }
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  public ShopItem buyFromUser(String username, Item item) {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(userShopRequest(username));
      Elements rows = content.select(":root > table > tbody > tr");
      List<Element> elems = Utils.merge(rows.stream().map(row -> {
        Elements cells = row.select(":root > td");
        return cells.stream().collect(Collectors.toList());
      }).collect(Collectors.toList()));
      Element itemElem = elems.stream().filter(cell -> {
        String name = cell.select(":root > b").first().text();
        return name.equals(item.getName());
      }).findFirst().get();
      String link = itemElem.select(":root > a").first().attr("href")
          .replaceAll("http://www.neopets.com", "");
      int cost = Utils.getNps(itemElem.text().split("Cost : ")[1]);
      loadBuyPageFromOwner(link, username);
      return new ShopItem(item.getName(), cost, link, 1);
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpResponse loadBuyPageFromOwner(String link, String username) {
    Logger.out.log(CATEGORY, "buy from owner: " + username);
    if (!link.startsWith("/")) {
      link = "/" + link;
    }
    return helper.get(link)
        .addHeader(Headers.referer("http://www.neopets.com/browseshop.phtml?owner=" + username))
        .send();
  }

  private HttpResponse loadBuyPage(ShopItem item, String link) {
    Logger.out.log(CATEGORY, "buy: " + item.getName());
    if (!link.startsWith("/")) {
      link = "/" + link;
    }
    return helper.get(link).addHeader(Headers.referer("http://www.neopets.com" + item.getLink()))
        .send();
  }

  private HttpRequest shopPageRequest(ShopItem shopItem) {
    Logger.out.log(CATEGORY, "shop item: " + shopItem.getName());
    return helper.get(shopItem.getLink());
  }

  private HttpRequest shopWizardRequest() {
    Logger.out.log(CATEGORY, "main");
    return helper.get("/market.phtml").addQueryParameter("type", "wizard");
  }

  private HttpRequest userShopRequest(String username) {
    Logger.out.log(CATEGORY, "user: " + username);
    return helper.get("/browseshop.phtml").addQueryParameter("owner", username);
  }

  public HttpRequest searchRequest(String name) {
    Logger.out.log(CATEGORY, "search: " + name);
    return helper.post("/market.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/market.phtml?type=wizard"))
        .addFormParameter("type", "process_wizard").addFormParameter("feedset", "0")
        .addFormParameter("shopwizard", name).addFormParameter("table", "shop")
        .addFormParameter("criteria", "exact").addFormParameter("min_price", "0")
        .addFormParameter("max_price", "999999");
  }
}
