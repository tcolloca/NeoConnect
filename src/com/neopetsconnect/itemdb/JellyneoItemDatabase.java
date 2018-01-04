package com.neopetsconnect.itemdb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.exceptions.ItemNotFoundException;
import com.neopetsconnect.main.Item;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class JellyneoItemDatabase {

	public static final String category = "JELLYNEO_DB";
	
	private static JellyneoItemDatabase instance;
	
	public synchronized static JellyneoItemDatabase getInstance() {
		if (instance == null) {
			instance = new JellyneoItemDatabase();
		}
		return instance;
	}
	
	public static void init() {
		JellyneoItemDatabase.getInstance();
	}
	
	private JellyneoItemDatabase() {
	}
	
	public static void main(String[] args) throws HttpHelperException, ItemNotFoundException {
		HttpHelper.logPath = "jelly/logs";
		HttpHelper.log = true;
		HttpHelper.storeCookies = false;
		Logger.out.log(category, JellyneoItemDatabase.getInstance().query("choco"));
	}
	
	public HttpResponse search(String query) throws HttpHelperException {
//		HttpHelper helper = new HttpHelper("items.jellyneo.net")
//		.useHttps()
//		.addDefaultHeader("Accept", "text/html")
//		.addDefaultHeader("Cache-Control", "no-cache");
//		
//		return helper.get("/search/?" + query).send();
		throw new RuntimeException("Jellyneo IDB has banned me.");
	}
	
	public int findPrice(String name) throws HttpHelperException, ItemNotFoundException {
		return getItem(name).getPrice().orElseThrow(() -> new ItemNotFoundException(name));
	}
	
	public Item getItem(String name) throws HttpHelperException, ItemNotFoundException {
		List<Item> items;
		try {
			items = query("name_type=3&name=" + URLEncoder.encode(name, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException();
		}
		if (items.isEmpty()) {
			throw new ItemNotFoundException(name);
		} else {
			return items.get(0);
		}
	}
	
	public List<Item> query(String query) throws HttpHelperException {
		String content = search(query).getContent().get();
		Document doc = Jsoup.parse(content);
		
		Element itemList = doc.body()
				.select(":root > div").get(3)
				.select(":root > div").first()
				.select(":root > ul.text-center").first();
		
		List<Item> items = new ArrayList<>();
		
		if (itemList == null) {
			return items;
		}
		
		itemList.select(":root > li").forEach(itemElem -> {
			String name = itemElem.select(":root > a").get(1).text();
			Optional<Integer> price = Optional.empty();
		
			Element priceElem = itemElem.select(":root > span.text-small").first();	
			if (priceElem != null) {
				price = Optional.of(Utils.getNps(priceElem.select(":root > a").first().text()));
			}
			items.add(new Item(name, price));
		});
		return items;
	}
	
	public List<Item> getStamps() throws HttpHelperException {
		return getCategory(31);
	}
	
	public List<Item> getMagicItems() throws HttpHelperException {
		return getCategory(2);
	}
	
	public List<Item> getBooks() throws HttpHelperException {
		return getCategory(5);
	}
	
	public List<Item> getSpaceBooks() throws HttpHelperException {
		return getCategory(37);
	}
	
	public List<Item> getCategory(int catNumber) throws HttpHelperException {
		List<Item> categoryItems = new ArrayList<>();
		List<Item> partial = query("cat[]=" + catNumber + "&limit=100");
		for (int i = 1; !partial.isEmpty(); i++) {
			categoryItems.addAll(partial);
			partial = query("cat[]=" + catNumber + "&limit=100&start=" + (i * 100));
		}
		return categoryItems;
	}
}