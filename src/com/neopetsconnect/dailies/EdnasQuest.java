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

public class EdnasQuest implements ConfigProperties {

	private final static String CATEGORY = EDNAS_QUEST;
	private final HttpHelper helper;
	private final ShopWizard shopWizard;
	
	public EdnasQuest(HttpHelper helper) {
		this.helper = helper;
		this.shopWizard = new ShopWizard(helper);
	}
	
	public static void main(String[] args) throws HttpHelperException {
		HttpHelper helper = Main.initHelper();	
		Main.handleSession(helper);		
		
		EdnasQuest daily = new EdnasQuest(helper);
		int cost = daily.completeQuest(5);
		Logger.out.log(CATEGORY, cost);
	}
	
	public int call() {
		int maxSpend = EDNAS_QUEST_DAILY_MAX_SPEND;
		int questsDone = DailyLog.props.getInt(QUESTS, "quests_done", 0);
		int spent = DailyLog.props.getInt(QUESTS, "spent", 0);
		try {
			if (spent <= maxSpend && questsDone < 10) {
				int wasted = completeQuest(SHOP_WIZ_SEARCH_TIMES);
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
	
	private int completeQuest(int searchTimes) throws HttpHelperException {
		List<Item> items = getQuestItems();
		int maxCost = getMaxCost(items.size());
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
	
	private int getMaxCost(int size) {
		switch (size) {
		case 1: 
			return EDNAS_QUEST_1_IT_MAX_COST;
		case 2: 
			return EDNAS_QUEST_2_IT_MAX_COST;
		case 3: 
			return EDNAS_QUEST_3_IT_MAX_COST;
		}
		throw new IllegalStateException("Ednas quests should have between 1 and 3 items.");
	}
	
	private List<Item> getQuestItems() throws HttpHelperException {
		String content = loadMain().getContent().get();
		Document doc = Jsoup.parse(content);
		Elements form = DomUtils.getContent(doc).select(":root > form");
		List<Element> itemElems;
		if (form.isEmpty()) {
			Elements centers = DomUtils.getContent(doc)
					.select(":root > center");
			if (centers.size() < 2) {
				Logger.out.log(EXCEPTION, content);
				throw new IllegalStateException("Ednas parsing failed.");
			}
			String dishName = centers.get(1).text();
			Logger.out.log(CATEGORY, "Dish name: " + dishName);
			content = askQuest(dishName).getContent().get();
			doc = Jsoup.parse(content);
			itemElems = DomUtils.getContent(doc)
					.select(":root > table > tbody > tr > td > b");
		} else {
			itemElems = DomUtils.getContent(doc)
					.select(":root > form > table > tbody > tr").get(2)
					.select(":root > td > table > tbody > tr > td > b");
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
		return helper.get("/halloween/witchtower.phtml")
				.send();
	}
	
	private HttpResponse askQuest(String name) throws HttpHelperException {
		return helper.post("/halloween/witchtower2.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/halloween/witchtower.phtml"))
				.addFormParameter("food_desc", name)
				.send();
	}
	
	private HttpResponse postItems() throws HttpHelperException {
		return helper.post("/halloween/witchtower2.phtml")
				.addFormParameter("type", "gotingredients")
				.addHeader(Headers.referer("http://www.neopets.com/halloween/witchtower.phtml"))
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.send();
	}
}
