package com.neopetsconnect.dailies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpResponse;
import com.logger.main.TimeUnits;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class BuriedTreasure implements ConfigProperties {

	private static final String CATEGORY = BURIED_TREASURE;

	private final HttpHelper helper;
	
	public BuriedTreasure(HttpHelper helper) {
		this.helper = helper;
	}
	
	public int call() {
		try {
			parseMain();
			int waitTime = parseBuriedTreasure();
			if(waitTime == 0) {
				int x = BURIED_TREASURE_X;
				int y = BURIED_TREASURE_Y;
				play(x, y);
				Logger.out.log(CATEGORY, String.format("Played (%d, %d).", x, y));	
			} else {
				Logger.out.logTime(CATEGORY, "Have to wait %.0f minutes.", waitTime, 
						TimeUnits.SECONDS, TimeUnits.MINUTES);	
				return waitTime;
			}
		} catch (HttpHelperException e) {
			throw new RuntimeException("Something when wrong.", e);
		}
		return DAILIES_REFRESH_FREQ;
	}

	private boolean parseMain() throws HttpHelperException {
		loadMain();
		return true;
	}
	
	private int parseBuriedTreasure() throws HttpHelperException {
		String content = loadBuriedTreasure().getContent().get();
		Document doc = Jsoup.parse(content);
		String text = DomUtils.getContent(doc).select(":root > div > center > p > b").text();
		if (text.trim().isEmpty()) {
			return 0;
		}
		return Integer.parseInt(Utils.between(text, "another", "minutes")) * 60;
	}
	
	private boolean play(int x, int y) throws HttpHelperException {
		return loadPlay(x, y).getStatusCode() == 200;
	}
	
	private HttpResponse loadMain() throws HttpHelperException {
		return helper.get("/pirates/buriedtreasure/index.phtml")
				.send();
	}
	
	private HttpResponse loadBuriedTreasure() throws HttpHelperException {
		return helper.get("/pirates/buriedtreasure/buriedtreasure.phtml?")
				.addHeader(Headers.referer("http://www.neopets.com/pirates/buriedtreasure/index.phtml"))
				.send();
	}
	
	private HttpResponse loadPlay(int x, int y) throws HttpHelperException {
		return helper.get("/pirates/buriedtreasure/buriedtreasure.phtml?" + x + "," + y)
				.addHeader(Headers.referer("http://www.neopets.com/pirates/buriedtreasure/buriedtreasure.phtml?"))
				.send();
	}
}