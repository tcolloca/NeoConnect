package com.neopetsconnect.stores;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.logger.main.TimeUnits;
import com.neopetsconnect.exceptions.NotEnoughNeopointsException;
import com.neopetsconnect.itemdb.ItemDatabase;
import com.neopetsconnect.main.Item;
import com.neopetsconnect.shopwizard.ShopItem;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;
import com.neopetsconnect.utils.captcha.CaptchaSolver;

public class NeopetsStore implements ConfigProperties {
	
	private static final int UNKNOWN_PRICE = 151000;
	
	final HttpHelper helper;
	final int id;

	public NeopetsStore(HttpHelper helper, int id) {
		this.helper = helper;
		this.id = id;
	}
	
	public List<ShopItem> getWorthyItems(int minDiff) throws HttpHelperException {
		return getWorthyItems(getItems(), minDiff);
	}
	
	public List<ShopItem> getWorthyItems(List<ShopItem> items, int minDiff) throws HttpHelperException {
		ItemDatabase itemDb = ItemDatabase.getInstance();
		Logger.out.log(STORE, items.size());
		return items.stream().collect(Collectors.toMap(item -> item, item -> {
			Item jellyItem = itemDb.getItem(item.getName());
			if (jellyItem != null && jellyItem.getPrice().isPresent()) {
				Logger.out.log(STORE, item.getName() + " " + (jellyItem.forceGetPrice() - item.forceGetPrice()));
				return jellyItem.forceGetPrice() - item.forceGetPrice();
			}
			Logger.out.log(STORE, item.getName() + " " + (UNKNOWN_PRICE - item.forceGetPrice()));
			return UNKNOWN_PRICE;
		}))
		.entrySet().stream().filter(e -> e.getValue() >= minDiff)
		.sorted(Map.Entry.<ShopItem, Integer>comparingByValue().reversed()) 
		.map(Map.Entry::getKey)
		.collect(Collectors.toList());
	}
	
	public List<ShopItem> getItems() throws HttpHelperException {
		List<ShopItem> items = new ArrayList<>();
		Document doc = Jsoup.parse(load().getContent().get());
		Element itemsForSale = DomUtils.getContent(doc)
		 	.select(":root > form[name=items_for_sale]").first();
		
		if (itemsForSale == null) {
			Logger.out.log(STORE, "Sold out!");
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
	}
	
	public boolean buyItem(ShopItem item) throws HttpHelperException, NotEnoughNeopointsException {
		Logger.out.logTimeStart("", TimeUnits.SECONDS);
		String content = loadHuggleLink(item.getLink()).getContent().get();
		Logger.out.logTimeEnd("", STORE, "Load huggle link: %.3f");
		Logger.out.logTimeStart("", TimeUnits.SECONDS);
		Document doc = Jsoup.parse(content);
		Logger.out.logTimeEnd("", STORE, "Parse huggle link: %.3f");
		
		int nps = Integer.parseInt(doc.getElementById("npanchor").text().replaceAll(",", ""));
		
		if (nps < item.forceGetPrice()) {
			throw new NotEnoughNeopointsException(
					"Not enough neopoints. Expected: " + item.forceGetPrice() + " has: " + nps);
		}
		
		String imgUrl = DomUtils.getContent(doc)
			.select(":root form > center > div > input").first()
			.attr("src");
		
		Logger.out.logTimeStart("", TimeUnits.SECONDS);
		int[] point = CaptchaSolver.solve(helper, imgUrl);
		Logger.out.logTimeEnd("", STORE, "Solve Captcha: %.3f");
		return parseBuyItemRequest(item.getLink(), item.forceGetPrice(), point);
	}
	
	private boolean parseBuyItemRequest(String huggleLink, int offer, int[] point) throws HttpHelperException {
		String content = buyItemRequest(huggleLink, offer, point).getContent().get();
		Document doc = Jsoup.parse(content);
		
		return DomUtils.getContent(doc)
			.select(":root font > b").first()
			.text().startsWith("The Shopkeeper says 'I accept your offer");
	}
	
	private HttpResponse buyItemRequest(String huggleLink, int offer, int[] point) throws HttpHelperException {
		return helper.post("/haggle.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com" + huggleLink))
				.addFormParameter("current_offer", String.valueOf(offer))
				.addFormParameter("x", String.valueOf(point[0]))
				.addFormParameter("y", String.valueOf(point[1]))
				.send();
	}
	
	private HttpResponse loadHuggleLink(String huggleLink) throws HttpHelperException {
		return helper.get(huggleLink)
				.addHeader(Headers.referer("http://www.neopets.com/objects.phtml?type=shop&obj_type=" + id))
				.send();
	}
	
	public HttpResponse load() throws HttpHelperException {
		return helper.get("/objects.phtml")
				.addQueryParameter("type", "shop")
				.addQueryParameter("obj_type", String.valueOf(id))
				.send();
	}
}
