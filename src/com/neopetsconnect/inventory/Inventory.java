package com.neopetsconnect.inventory;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.exceptions.ItemNotFoundException;
import com.neopetsconnect.exceptions.ShopWizardBannedException;
import com.neopetsconnect.main.Item;
import com.neopetsconnect.main.Main;
import com.neopetsconnect.shopwizard.ShopWizard;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class Inventory implements ConfigProperties {

	public static final String category = INVENTORY;
	
	private final HttpHelper helper;
	
	public static void main(String[] args) throws HttpHelperException {
		HttpHelper helper = Main.initHelper();	
		Main.handleSession(helper);
		
		Inventory inv = new Inventory(helper);
		inv.organize(1000);
	}
	
	public Inventory(HttpHelper helper) {
		this.helper = helper;
	}
	
	public void organize(int minPrice) throws HttpHelperException {
		ShopWizard wiz = new ShopWizard(helper);
		Map<String, List<InventoryItem>> items = getItems().entrySet().stream()
				.filter(entry -> !entry.getValue().get(0).isBlocked())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		Map<String, List<InventoryItem>> discard = items.entrySet().stream().filter(entry -> {
			try {
				Logger.out.log(category, "Finding price for " + entry.getKey());
				int price = wiz.findPrice(entry.getKey(), 3);
				Logger.out.log(category, "Price for \"" + entry.getKey() + "\" is " + price);
				Utils.sleep(2);
				return price < minPrice;
				} catch (ItemNotFoundException e) {
					return false;
				} catch (ShopWizardBannedException e) {
					return false;
				} catch (Exception e) {
					throw new RuntimeException("Failed when empting inventory", e);
				}
			})
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		Map<String, List<InventoryItem>> toShop = new HashMap<>(items);
		toShop.entrySet().removeAll(discard.entrySet());
		moveToShop(Utils.merge(toShop.values()));
		discard(Utils.merge(discard.values()));
	}
	
	public Map<String, List<InventoryItem>> getItems() throws HttpHelperException {
		Map<String, List<InventoryItem>> items = new HashMap<>();
		Document doc = Jsoup.parse(loadInventory().getContent().get());
		Element content =  DomUtils.getContent(doc)
				.select(":root > div[class=contentModule]").first();
		if (content == null) {
			return items;
		}
		Elements itemElems = content.select(":root table > tbody > tr").get(1)
				.select(":root > td > table > tbody > tr > td");
		itemElems.forEach(itemElem -> {
			String name = itemElem.ownText();
			Element attributes = itemElem.select(":root > span").first();
			boolean isBlocked = false; 
			if (attributes != null) {
				isBlocked = attributes.select(":root > span").stream().filter(elem -> {
					String style = elem.attr("style");
					return style.contains("blue") || style.contains("red") || style.contains("darkred");
				}).count() > 0;
			}
			items.putIfAbsent(name, new ArrayList<>());
			items.get(name).add(new InventoryItem(name, isBlocked));
		});
		return items;
	}
	
	public <T extends Item> void moveToSdb(T item) throws HttpHelperException {
		moveToSdb(Arrays.asList(item));
	}
	
	public <T extends Item> void moveToShop(T item) throws HttpHelperException {
		moveToShop(Arrays.asList(item));
	}
	
	public <T extends Item> void discard(T item) throws HttpHelperException {
		discard(Arrays.asList(item));
	}
	
	public <T extends Item> void moveToSdb(List<T> items) throws HttpHelperException {
		quickStock(items, Action.DEPOSIT);
	}
	
	public <T extends Item> void moveToShop(List<T> items) throws HttpHelperException {
		quickStock(items, Action.STOCK);
	}
	
	public <T extends Item> void discard(List<T> items) throws HttpHelperException {
		quickStock(items, Action.DISCARD);
	}
	
	public <T extends Item> void quickStock(List<T> items, Action action) throws HttpHelperException {
		quickStock(items.stream().collect(Collectors.groupingBy(Item::getName))
				.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream()
                		.map(v -> action).collect(Collectors.toList()))));
	}
	
	public void quickStock(Map<String, List<Action>> actionsMap) throws HttpHelperException {
		Map<String, List<QuickStockItem>> itemLists = getQuickStockItems();
		try {
			HttpRequest req = helper.post("/process_quickstock.phtml")
					.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
					.addHeader(Headers.referer("http://www.neopets.com/quickstock.phtml"))
					.addFormParameter("buyitem", 0);
			
			for (Entry<String, List<QuickStockItem>> itemEntry : itemLists.entrySet()) {
				String name = itemEntry.getKey();
				List<QuickStockItem> items = itemEntry.getValue();
				List<Action> actions = actionsMap.get(name);
				int i;
				for (i = 0; actions != null && i < actions.size(); i++) {
					req.addFormParameter("radio_arr" + items.get(i).getRowId(), actions.get(i).getString());
				}
				for (i = 0; i < itemEntry.getValue().size(); i++) {
					req.addFormParameter("id_arr" + items.get(i).getRowId(), items.get(i).getId());
				}
			}
			
			req.send();
		} catch (HttpHelperException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Map<String, List<QuickStockItem>> getQuickStockItems() throws HttpHelperException {
		Document doc = Jsoup.parse(loadQuickStock().getContent().get());
		Map<String, List<QuickStockItem>> items = new HashMap<>();
		Element quickstock = DomUtils.getContent(doc)
				.select(":root form[name=quickstock]").first();
		if (quickstock == null) {
			return items;
		}
		List<Element> itemElems = quickstock.select(":root > table > tbody > tr").stream()
				.filter(elem -> elem.attr("bgcolor").toLowerCase().startsWith("#ffff"))
				.collect(Collectors.toList());
		itemElems.forEach(itemElem -> {
			Elements cols = itemElem.select(":root > td");
			if (cols.attr("colspan").isEmpty()) {
				String name = cols.get(0).text();
				Element itemInfo = itemElem.select(":root > input").first();
				String id = itemInfo.attr("value");
				String rowId = itemInfo.attr("name").split("id_arr")[1];
				items.putIfAbsent(name, new ArrayList<>());
				items.get(name).add(new QuickStockItem(name, id, rowId));
			}
		});
		return items;
	}
	
	public HttpResponse loadQuickStock() throws HttpHelperException {
		return helper.get("/quickstock.phtml")
				.addHeader(Headers.referer("http://www.neopets.com/inventory.phtml"))
				.send();
	}
	
	private HttpResponse loadInventory() throws HttpHelperException {
		return helper.get("/inventory.phtml")
				.addHeader(Headers.referer("http://www.neopets.com/index.phtml"))
				.send();
	}
	
	private enum Action {
		STOCK("stock"), DEPOSIT("deposit"), DISCARD("discard");
		
		private final String string;

		private Action(String string) {
			this.string = string;
		}

		String getString() {
			return string;
		}
	}
}
