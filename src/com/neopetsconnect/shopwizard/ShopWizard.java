package com.neopetsconnect.shopwizard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.exceptions.FaerieQuestException;
import com.neopetsconnect.exceptions.ItemNotFoundException;
import com.neopetsconnect.exceptions.ShopWizardBannedException;
import com.neopetsconnect.main.Item;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class ShopWizard implements ConfigProperties {

	private final HttpHelper helper;
	
	public ShopWizard(HttpHelper helper) {
		this.helper = helper;
	}
	
	public HttpResponse search(String name) throws HttpHelperException {
		return helper.post("/market.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/market.phtml?type=wizard"))	
				.addFormParameter("type", "process_wizard")
				.addFormParameter("feedset", "0")
				.addFormParameter("shopwizard", name)
				.addFormParameter("table", "shop")
				.addFormParameter("criteria", "exact")
				.addFormParameter("min_price", "0")
				.addFormParameter("max_price", "99999")
				.send();
	}
	
	public List<ShopItem> buyItem(List<ShopItem> shopItems, int amount) throws HttpHelperException, ItemNotFoundException {
		List<ShopItem> bought = new ArrayList<>();
		for (int i = 0; i < shopItems.size() && i < 3 * amount; i++) {
			try {
				for (int j = 0; j < shopItems.get(i).getAvailable() && bought.size() < amount; j++) {
					Logger.out.log(SHOP_WIZARD, "Buying item: " + shopItems.get(i));
					if (buyItem(shopItems.get(i))) {
						bought.add(shopItems.get(i));
					} else {
						break;
					}
				}
			} catch (HttpHelperException e) {
				throw new RuntimeException("Failed when buying item from list.", e);
			}
		}
		if (bought.size() < amount) {
			throw new ItemNotFoundException("Couldn't buy all of " + shopItems.get(0).getName());
		}
		return bought;
	}
	
	public boolean buyItem(ShopItem shopItem) throws HttpHelperException {
		String content = loadShopPage(shopItem).getContent().get();
		Document doc = Jsoup.parse(content);
		try {
			Element contentElem = DomUtils.getContent(doc);
			if (contentElem.text().contains("her quest")) {
				throw new FaerieQuestException("You have a faerie quest!");
			}
			String link = contentElem
					.select(":root > div").get(3)
					.select(":root > table > tbody > tr > td > a").get(0)
					.attr("href");
			loadBuyPage(shopItem, link);
			return true; // TODO 
		} catch (Exception e) {
			Logger.out.log(EXCEPTION, content.replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
			throw new RuntimeException("Failed when buying: " + shopItem.getName(), e);
		}
	}
	
	public List<ShopItem> findItem(String name, int iterations) throws HttpHelperException, ItemNotFoundException, ShopWizardBannedException {
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
	
	public List<ShopItem> findItem(String name) throws HttpHelperException, ItemNotFoundException, ShopWizardBannedException {
		String content = search(name).getContent().get();
		Document doc = Jsoup.parse(content);
		Element shopWizContent = DomUtils.getContent(doc)
			.select(":root > div").get(1);
		Elements tables = shopWizContent.select(":root > table");
		if (!tables.isEmpty()) {
			if (tables.size() == 1) {
				throw new ItemNotFoundException(name);
			}
			List<Element> itemElems = tables.get(1).select(":root > tbody > tr").stream().collect(Collectors.toList());
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
	}
	
	public int findPrice(String name, int iterations) throws HttpHelperException, ItemNotFoundException, ShopWizardBannedException {
		return findItem(name, iterations).get(0).forceGetPrice();
	}
	
	public int findPrice(String name) throws HttpHelperException, ItemNotFoundException, ShopWizardBannedException {
		return findPrice(name, 1);
	}
	
	private HttpResponse loadBuyPage(ShopItem item, String link) throws HttpHelperException {
		if (!link.startsWith("/")) {
			link = "/" + link;
		}
		return helper.get(link)
				.addHeader(Headers.referer("http://www.neopets.com" + item.getLink()))
				.send();
	}
	
	private HttpResponse loadShopPage(ShopItem shopItem) throws HttpHelperException {
		return helper.get(shopItem.getLink())
				.send();
	}
	
	public  Map<Item, List<ShopItem>> buyItems(List<Item> items, int times) {
		return buyShopItems(findItems(items, times));
	}
	
	public Map<Item, List<ShopItem>> buyIfWorthIt(List<Item> items, int maxCost, int times) {
		Map<Item, List<ShopItem>> shopItems = findItems(items, times);
		
		int totalCost = shopItems.entrySet().stream()
				.mapToInt(entry -> entry.getValue().get(0).forceGetPrice() * entry.getKey().getAmount())
				.sum();
		Logger.out.log(SHOP_WIZARD, "Total cost: %d, max cost: %d.", totalCost, maxCost);
		if (totalCost > maxCost) {
			Logger.out.log(SHOP_WIZARD, "Too expensive :/");
			return new HashMap<>();
		}
		
		return buyShopItems(shopItems);
//				if (bought.size() == entry.getKey().getAmount()) {
//					Logger.out.log(KITCHEN_QUEST, "Bought: " + bought.get(0));
//				} else {
//					Logger.out.log(KITCHEN_QUEST, "Couldn't buy all of: " + entry.getValue().get(0).getName());
//				}
	}
	
	private Map<Item, List<ShopItem>> findItems(List<Item> items, int searchTimes) {
		return items.stream().collect(Collectors.toMap(item -> item, item -> {
			try {
				Logger.out.log(SHOP_WIZARD, "Finding: " + item.getName());
				return findItem(item.getName(), searchTimes);
			} catch (ItemNotFoundException | HttpHelperException | ShopWizardBannedException e) {
				Logger.out.log(SHOP_WIZARD, "Failed finding item: " + item.getName());
				throw new RuntimeException(e);
			}
		}));
	}
	
	private Map<Item, List<ShopItem>> buyShopItems(Map<Item, List<ShopItem>> items) {
		return items.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, entry -> {
					try {
						Logger.out.log(SHOP_WIZARD, "Buying: " + entry.getKey());
						return buyItem(entry.getValue(), entry.getKey().getAmount());
					} catch (HttpHelperException | ItemNotFoundException e) {
						throw new RuntimeException("Failed buying item.", e);
					}
				}));
	}
}
