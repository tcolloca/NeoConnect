package com.neopetsconnect.dailies.trainingschool;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
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
import com.logger.main.TimeUnits;
import com.neopetsconnect.dailies.status.CompletedStatus;
import com.neopetsconnect.dailies.status.NoneStatus;
import com.neopetsconnect.dailies.status.PendingPayStatus;
import com.neopetsconnect.dailies.status.Status;
import com.neopetsconnect.dailies.status.WaitingStatus;
import com.neopetsconnect.main.Item;
import com.neopetsconnect.main.Main;
import com.neopetsconnect.shopwizard.ShopWizard;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class TrainingSchool implements ConfigProperties {

	private static final String CATEGORY = TRAINING_SCHOOL;
	private final HttpHelper helper;
	private final ShopWizard shopWizard;
	
	public TrainingSchool(HttpHelper helper) {
		this.helper = helper;
		this.shopWizard = new ShopWizard(helper);
	}
	
	public static void main(String[] args) throws HttpHelperException {
		HttpHelper helper = Main.initHelper();	
		Main.handleSession(helper);	
		
		new TrainingSchool(helper).call();
	}
	
	public int call() throws HttpHelperException {
		Status status;
		Status prev = null;
		while (!((status = getStatus()) instanceof WaitingStatus) && !status.equals(prev)) {
			Logger.out.log(CATEGORY, status.getClass().getSimpleName());
			if (status instanceof WaitingStatus) {
				Logger.out.log(CATEGORY, status);
				return ((WaitingStatus) status).getWaitTime().toSecondOfDay();
			}
			if (status instanceof CompletedStatus) {
				StatIncrease statIncrease = parseComplete();
				if (statIncrease != null) {
					Logger.out.log(CATEGORY, statIncrease);
				}
			}
			if (status instanceof NoneStatus) {
				train(STAT_TYPE);
			}
			if (status instanceof PendingPayStatus) {
				pay((PendingPayStatus) status);
			}
		}
		if (status.equals(prev)) {
			throw new RuntimeException("Looping at training school");
		}
		Logger.out.logTime(CATEGORY, "Have to wait %.0f minutes.", 
				((WaitingStatus) status).getWaitTime().toSecondOfDay(),
				TimeUnits.SECONDS, TimeUnits.MINUTES);
		return ((WaitingStatus) status).getWaitTime().toSecondOfDay();
	}
	
	private void pay(PendingPayStatus status) throws HttpHelperException {
		List<Item> codestones = ((PendingPayStatus) status).getItems();
		Logger.out.log(CATEGORY, "Needed: " + codestones.stream().map(it -> it.getName())
				.collect(Collectors.toList()));
		if(shopWizard.buyItems(codestones, SHOP_WIZ_SEARCH_TIMES).size() == codestones.size()) {
			Logger.out.log(CATEGORY, "Bought all.");
			if(pay()) {
				Logger.out.log(CATEGORY, "Started training.");
			}
		}
	}
	
	private Status getStatus() throws HttpHelperException {
		String content = loadStatus().getContent().get();
		Document doc = Jsoup.parse(content);
		Elements pets = DomUtils.getContent(doc).select(":root > div").get(1)
				.select(":root > table > tbody > tr");
		int index = 6;
		for (int i = 0; i < pets.size(); i += 2) {
			if (pets.get(i).select(":root > td").text().contains("jimtry")) {
				index = i;
				break;
			}
		}
		Element statusCol = pets.get(index + 1).select(":root > td").get(1);
		String status = statusCol.text().trim();
		if (status.equals("Course Finished!")) {
			return new CompletedStatus();
		} else if (status.isEmpty()) {
			return new NoneStatus();
		} else if (status.contains("This course has not been paid for yet")) {
			List<Item> codestones = statusCol.select(":root > p > b").stream()
					.map(elem -> new Item(elem.text().trim()))
					.collect(Collectors.toList());
			return new PendingPayStatus(codestones);
		} else {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H 'hrs', m 'minutes', s 'seconds'");
			TemporalAccessor time = formatter.parse(status.split(":")[1].trim());
			return new WaitingStatus(Utils.toLocalTime(time));
		}
	}
	
	private StatIncrease parseComplete() throws HttpHelperException {
		String content = loadComplete().getContent().get();
		Document doc = Jsoup.parse(content);
		String text = doc.text();
		String statTypeStr = text.split("now has increased ")[1].split("!!!")[0].trim();
		int amount = text.contains("SUPER BONUS") ? Integer.parseInt(Utils.between(text, "up", "points")) : 1;
		return new StatIncrease(StatType.fromName(statTypeStr), amount);
	}
	
	private boolean train(String statType) throws HttpHelperException {
		// TODO : Check.
		loadCourses();
		return loadTrain(statType).getStatusCode() == 200;
	}
	
	private boolean pay() throws HttpHelperException {
		return loadPay().getStatusCode() == 200;
	}
	
	private HttpResponse loadTrain(String statType) throws HttpHelperException {
		return helper.post("/island/process_training.phtml")
				.addHeader(Headers.referer("http://www.neopets.com/island/training.phtml?type=courses"))
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addFormParameter("type", "start")
				.addFormParameter("pet_name", "jimtry")
				.addFormParameter("course_type", Character.toUpperCase(statType.charAt(0)) 
						+ statType.toLowerCase().substring(1))
				.send();
	}
	
	private HttpResponse loadPay() throws HttpHelperException {
		return helper.get("/island/process_training.phtml")
				.addHeader(Headers.referer("http://www.neopets.com/island/training.phtml?type=status"))
				.addQueryParameter("type", "pay")
				.addQueryParameter("pet_name", "jimtry")
				.send();
	}
	
	private HttpResponse loadComplete() throws HttpHelperException {
		return helper.post("/island/process_training.phtml")
				.addHeader(Headers.referer("http://www.neopets.com/island/training.phtml?type=status"))
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addFormParameter("type", "complete")
				.addFormParameter("pet_name", "jimtry")
				.send();
	}
	
	private HttpResponse loadCourses() throws HttpHelperException {
		return helper.get("/island/training.phtml")
				.addQueryParameter("type", "courses")
				.send();
	}
	
	private HttpResponse loadStatus() throws HttpHelperException {
		return helper.get("/island/training.phtml")
				.addQueryParameter("type", "status")
				.send();
	}
}
