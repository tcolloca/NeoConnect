package com.neopetsconnect.dailies;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpResponse;
import com.logger.main.TimeUnits;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class Turmaculus implements ConfigProperties {

	private static final String CATEGORY = TURMACULUS;
	private final HttpHelper helper;
	
	public Turmaculus(HttpHelper helper) {
		this.helper = helper;
	}
	
	public int call() {
		try {
			if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
				LocalDateTime nextTime = getNextAwakeTime();
				LocalDateTime now = Utils.neopetsNow();
				Logger.out.log(CATEGORY, "Next time: " + nextTime);
				Logger.out.log(CATEGORY, "Now: " + now);
				if (nextTime.toLocalDate().equals(now.toLocalDate())) {
					if (nextTime.getHour() == now.getHour()) {
						boolean aux = HttpHelper.log;
						HttpHelper.log = true;
						visit();
						HttpHelper.log = aux;
						Logger.out.log(CATEGORY, "Visited: " + now);
						DailyLog.props.addBoolean(CATEGORY, "done", true);
					} else if (nextTime.isAfter(now)) {
						int returnTime =  nextTime.toLocalTime().toSecondOfDay() - now.toLocalTime().toSecondOfDay();
						Logger.out.logTime(CATEGORY, "Return in: %.2f hours.", returnTime, TimeUnits.SECONDS, TimeUnits.HOURS);
						return returnTime;
					}
				} else if (nextTime.isBefore(now)) {				
					DailyLog.props.addBoolean(CATEGORY, "done", true);
				}
			}
			Logger.out.log(CATEGORY, "Done.");
		} catch (HttpHelperException e) {
			throw new RuntimeException("Something when wrong.", e);
		}
		return DAILIES_REFRESH_FREQ;
	}
	
	private LocalDateTime getNextAwakeTime() throws HttpHelperException {
		String content = loadTimes().getContent().get();
		Document doc = Jsoup.parse(content);
		Elements dates = doc.select(":root > body > div[align=center] > table[class=main]"
				+ " > tbody > tr > td > div[align=center] > font[class=title]");
		String[] relevant = Utils.between(dates.toString(), "title\">", "NST").split(":");
		String dateStr = relevant[0].trim();
		String timeRawStr = relevant[1].split("or")[0].trim();
		String timeStr = timeRawStr.replaceAll("(am|AM)", "" + "\\-")
							 .replaceAll("(pm|PM)", "" + "\\+")
							 .replaceAll("[^\\d+-]", "")
							 .replaceAll("\\-", "AM")
		 				     .replaceAll("\\+", "PM");
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d['st']['nd']['rd']['th'] yyyy", Locale.ENGLISH);
		LocalDate date = LocalDate.parse(dateStr, dateFormatter);
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("ha");
		TemporalAccessor time = timeFormatter.parse(timeStr);
		return LocalDateTime.of(date, Utils.toLocalTime(time));
	}
	
	private void visit() throws HttpHelperException {
		String prev = makeActive(TURMACULUS_PET_NAME);
		botherTurmaculus();
		makeActive(prev);
	}
	
	private void botherTurmaculus() throws HttpHelperException {
		loadMain();
		loadHitTurmaculus();
	}
	
	private String makeActive(String petName) throws HttpHelperException {
		String active = getActive(loadQuickRef());
		loadMakeActive(petName);
		return active;
	}
	
	private String getActive(HttpResponse resp) {
		Document doc = Jsoup.parse(resp.getContent().get());
		String script = doc.getElementsByClass("active_pet").get(0).select(":root > a").attr("onClick");
		return Utils.between(script, "togglePetDetails\\('", "'\\)");
	}
	
	private HttpResponse loadHitTurmaculus() throws HttpHelperException {
		return helper.post("/medieval/process_turmaculus.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/medieval/turmaculus.phtml"))
				.addFormParameter("type", "wakeup")
				.addFormParameter("active_pet", TURMACULUS_PET_NAME)
				.addFormParameter("wakeup", 8)
				.send();
	}
	
	private HttpResponse loadMakeActive(String petName) throws HttpHelperException {
		return helper.get("/process_changepet.phtml?new_active_pet=" + petName)
				.addHeader(Headers.referer("http://www.neopets.com/quickref.phtml"))
				.send();
	}
	
	private HttpResponse loadQuickRef() throws HttpHelperException {
		return helper.get("/quickref.phtml")
				.send();
	}
	
	private HttpResponse loadTimes() throws HttpHelperException {
		return helper.get("/~Brownhownd")
				.send();
	}
	
	private HttpResponse loadMain() throws HttpHelperException {
		return helper.get("/medieval/turmaculus.phtml")
				.send();
	}
}