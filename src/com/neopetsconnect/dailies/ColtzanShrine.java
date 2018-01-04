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

public class ColtzanShrine implements ConfigProperties {

	private static final String CATEGORY = COLTZANS_SHRINE;
	
	private final HttpHelper helper;
	
	public ColtzanShrine(HttpHelper helper) {
		this.helper = helper;
	}
	
	public int call() {
		try {
			if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
				parseMain();
				if(visit()) {
					Logger.out.log(CATEGORY, "Visited :)");
					DailyLog.props.addBoolean(CATEGORY, "done", true);
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

	private boolean parseMain() throws HttpHelperException {
		loadMain();
		return true;
	}
	
	private HttpResponse loadMain() throws HttpHelperException {
		return helper.get("/desert/shrine.phtml")
				.send();
	}
	
	private boolean visit() throws HttpHelperException {
		return !DomUtils.getContent(Jsoup.parse(loadVisit().getContent().get()))
				.text().contains("Maybe you should wait a while before visiting the shrine again....");
	}
	
	private HttpResponse loadVisit() throws HttpHelperException {
		return helper.post("/desert/shrine.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/desert/shrine.phtml"))
				.addFormParameter("type", "approach")
				.send();
	}
}

