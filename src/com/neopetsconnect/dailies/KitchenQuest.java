package com.neopetsconnect.dailies;

import java.util.List;
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
import com.neopetsconnect.main.Item;
import com.neopetsconnect.main.Main;
import com.neopetsconnect.shopwizard.ShopWizard;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;

public class KitchenQuest implements ConfigProperties {

	private final static String CATEGORY = KITCHEN_QUEST;
	private final HttpHelper helper;
	private final ShopWizard shopWizard;
	
	public KitchenQuest(HttpHelper helper) {
		this.helper = helper;
		this.shopWizard = new ShopWizard(helper);
	}
	
	public static void main(String[] args) throws HttpHelperException {
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
		try {
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
		} catch (HttpHelperException e) {
			throw new RuntimeException("Something when wrong.", e);
		}
		return DAILIES_REFRESH_FREQ;
	}
	
	private int completeQuest(int maxCost, int searchTimes) throws HttpHelperException {
		List<Item> items = getQuestItems();
		Logger.out.log(CATEGORY, "Items: " + String.join(", ", items.stream()
				.map(it -> it.getName()).collect(Collectors.toList())));		
		int wasted = shopWizard.buyIfWorthIt(items, maxCost, searchTimes).entrySet().stream()
				.mapToInt(entry -> entry.getValue().get(0).forceGetPrice())
				.sum();
		if (wasted > 0) {
			return giveItems() ? wasted : 0;
		}
		return wasted;
	}
	
	private List<Item> getQuestItems() throws HttpHelperException {
		String content = loadMain().getContent().get();
		Document doc = Jsoup.parse(content);
		Elements centers = DomUtils.getContent(doc).select(":root > center");
		List<Element> itemElems;
		if (centers.size() < 2) {
			itemElems = DomUtils.getContent(doc)
				.select(":root > form > table > tbody > tr").get(2)
				.select(":root > td").get(1)
				.select(":root > b");
		} else {
			String dishName = DomUtils.getContent(doc)
					.select(":root > center").get(1).text();
			Logger.out.log(CATEGORY, "Dish name: " + dishName);
			content = askQuest(dishName).getContent().get();
			doc = Jsoup.parse(content);
			itemElems = DomUtils.getContent(doc)
					.select(":root > table > tbody > tr > td > b");
		}
		return itemElems.stream().map(itemElem -> {
			String itemName = itemElem.text();
			return new Item(itemName);
		}).collect(Collectors.toList());
	}
	
	private boolean giveItems() throws HttpHelperException {
		loadMain();
		String content = postItems().getContent().get();
		Document doc = Jsoup.parse(content);
		return DomUtils.getContent(doc)
				.select(":root > center").get(0)
				.select(":root > p > b").text().contains("successfully");
	}
	
	private HttpResponse loadMain() throws HttpHelperException {
		return helper.get("/island/kitchen.phtml")
				.send();
	}
	
	private HttpResponse askQuest(String name) throws HttpHelperException {
		return helper.post("/island/kitchen2.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/island/kitchen.phtml"))
				.addFormParameter("food_desc", name)
				.send();
	}
	
	private HttpResponse postItems() throws HttpHelperException {
		return helper.post("/island/kitchen2.phtml")
				.addFormParameter("type", "gotingredients")
				.addHeader(Headers.referer("http://www.neopets.com/island/kitchen.phtml"))
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.send();
	}
}
