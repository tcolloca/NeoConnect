package com.neopetsconnect.dailies;

import org.jsoup.Jsoup;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;

public class YeOldeFishingVortex implements ConfigProperties {

	private static final String CATEGORY = FISHING_VORTEX;
	
	private final HttpHelper helper;
	
	public YeOldeFishingVortex(HttpHelper helper) {
		this.helper = helper;
	}
	
	public int call() {
		try {
			if (!DailyLog.props.getBoolean(FISHING_VORTEX, "done", false)) {
				parseMain();
				if(fish()) {
					Logger.out.log(CATEGORY, "Fished :)");
					DailyLog.props.addBoolean(FISHING_VORTEX, "done", true);
				} else {
					Logger.out.log(CATEGORY, "Have to wait :(");
				}
			} else {
				Logger.out.log(CATEGORY, "Done.");
			}
		} catch (HttpHelperException e) {
			throw new RuntimeException("Something when wrong.", e);
		}
		return DAILIES_REFRESH_FREQ;
	}
	
	private boolean fish() throws HttpHelperException {
		return !DomUtils.getContent(Jsoup.parse(loadFishing().getContent().get()))
				.text().contains("patient");
	}
	
	private boolean parseMain() throws HttpHelperException {
		loadMain();
		return true;
	}
	
	private HttpResponse loadFishing() throws HttpHelperException {
		return helper.post("/water/fishing.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/water/fishing.phtml"))
				.addFormParameter("go_fish", 1)
				.send();
	}
	
	private HttpResponse loadMain() throws HttpHelperException {
		return helper.get("/water/fishing.phtml")
				.send();
	}
}

