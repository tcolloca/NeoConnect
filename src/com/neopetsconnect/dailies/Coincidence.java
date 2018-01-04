package com.neopetsconnect.dailies;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpResponse;
import com.logger.main.TimeUnits;
import com.neopetsconnect.dailies.status.CompletedStatus;
import com.neopetsconnect.dailies.status.NoneStatus;
import com.neopetsconnect.dailies.status.PendingPayStatus;
import com.neopetsconnect.dailies.status.Status;
import com.neopetsconnect.dailies.status.WaitingStatus;
import com.neopetsconnect.main.Item;
import com.neopetsconnect.main.Main;
import com.neopetsconnect.shopwizard.ShopItem;
import com.neopetsconnect.shopwizard.ShopWizard;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class Coincidence implements ConfigProperties {

	private final static String CATEGORY = COINCIDENCE;
	
	private final HttpHelper helper;
	private final ShopWizard shopWizard;
	
	public Coincidence(HttpHelper helper) {
		this.helper = helper;
		this.shopWizard = new ShopWizard(helper);
	}
	
	public static void main(String[] args) throws HttpHelperException {
		HttpHelper helper = Main.initHelper();	
		Main.handleSession(helper);		
		
		Coincidence daily = new Coincidence(helper);
		daily.call();
	}
	
	public int call() {
		try {
			Status status;
			Status prev = null;
			while (!((status = getStatus()) instanceof WaitingStatus) && !status.equals(prev)) {
				Logger.out.log(CATEGORY, status.getClass().getSimpleName());
				if (status instanceof CompletedStatus) {
					Logger.out.log(CATEGORY, "Gave items :)");
					giveItems();
				}
				if (status instanceof NoneStatus) {
					Logger.out.log(CATEGORY, "Asked new quest.");
					askQuest();
				}
				if (status instanceof PendingPayStatus) {
					pay((PendingPayStatus) status);
				}
			}
			if (status.equals(prev)) {
				throw new RuntimeException("Looping at training school");
			}
			Logger.out.logTime(CATEGORY, "Have to wait %.0f hours.", 
					((WaitingStatus) status).getWaitTime().toSecondOfDay(),
					TimeUnits.SECONDS, TimeUnits.HOURS);
			return ((WaitingStatus) status).getWaitTime().toSecondOfDay();
		} catch (HttpHelperException e) {
			throw new RuntimeException("Something when wrong.", e);
		}
	}
	
	private void pay(PendingPayStatus status) throws HttpHelperException {
		List<Item> items = ((PendingPayStatus) status).getItems();
		Logger.out.log(CATEGORY, "Needed: " + items);
		Map<Item, List<ShopItem>> bought = shopWizard.buyIfWorthIt(items,
				COINCIDENCE_MAX_SPEND, SHOP_WIZ_SEARCH_TIMES);
		if (bought.isEmpty()) {
			sayNo(((PendingPayStatus) status).getKey());
		}
		boolean succeeded = bought.entrySet().stream()
				.map(entry -> entry.getKey().getAmount() == entry.getValue().size())
				.reduce(true, (x, y) -> x && y);
		if (!succeeded) {
			Logger.out.log(CATEGORY, "Couldn't buy all :|");
		}
	}
	
	private Status getStatus() throws HttpHelperException {
		String content = loadMain().getContent().get();
		Document doc = Jsoup.parse(content);
		Element questItems = doc.getElementById("questItems");
		Element handItemsForm = doc.getElementById("shipQuest");
		if (questItems != null) {
			String script = DomUtils.getContent(doc).select(":root > script").get(2).toString();
			String _ref_ck = Utils.between(script, "_ref_ck: \"", "\"").trim();
			List<Item> items = questItems.select(":root > tbody > tr > td")
					.stream().map(elem -> {
						String[] aux = elem.text().split("x");
						String name = aux[0].trim();
						int amount = Integer.parseInt(aux[1].trim());
						return new Item(name).setAmount(amount);
					}).collect(Collectors.toList());
			return new PendingPayStatus(items, _ref_ck);
		} else if (handItemsForm != null) {
			return new CompletedStatus();
		} else {
			String text = DomUtils.getContent(doc).text();
			if (text.contains("RESEARCH COMPLETE IN")) {
				Element clock = DomUtils.getContent(doc).select(":root > div > script").get(0);
				String timeStr = Utils.between(clock.toString(), "\\{", "tick").replaceAll("(\r|\n|\t| )", "");
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'hours':H,'minutes':m,'seconds':s,");
				TemporalAccessor time = formatter.parse(timeStr);
				return new WaitingStatus(Utils.toLocalTime(time));
			}
			return new NoneStatus();
		}
		// TODO : Wait
	}
	
	private boolean askQuest() throws HttpHelperException {
		return loadAskQuest().getStatusCode() == 200;
	}
	
	private boolean sayNo(String key) throws HttpHelperException {
		return loadSayNo(key).getStatusCode() == 200;
	}
	
	private boolean giveItems() throws HttpHelperException {
		return postItems().getStatusCode() == 200;
	}
	
	private HttpResponse loadAskQuest() throws HttpHelperException {
		return helper.get("/magma/portal/ship.phtml")
				.addHeader(Headers.referer("http://www.neopets.com/space/coincidence.phtml"))
				.send();
	}
	
	private HttpResponse loadSayNo(String _ref_ck) throws HttpHelperException {
		return helper.post("/magma/portal/ajax/cancelQuest.php")
				.addHeader(Headers.referer("http://www.neopets.com/space/coincidence.phtml"))
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addFormParameter("_ref_ck", _ref_ck)
				.send();
	}
	
	private HttpResponse postItems() throws HttpHelperException {
		return helper.post("/space/coincidence.phtml")
				.addHeader(Headers.referer("http://www.neopets.com/space/coincidence.phtml"))
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addFormParameter("showRe", 1)
				.send();
	}
	
	private HttpResponse loadMain() throws HttpHelperException {
		return helper.get("/space/coincidence.phtml")
				.send();
	}
}
