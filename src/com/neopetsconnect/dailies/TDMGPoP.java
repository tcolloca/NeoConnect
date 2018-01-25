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

public class TDMGPoP implements ConfigProperties {

	private static final String CATEGORY = TDMGPOP;
	private final HttpHelper helper;
	
	public TDMGPoP(HttpHelper helper) {
		this.helper = helper;
	}
	
	public int call() {
		try {
			if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
				parseMain();
				if(talkToThePlushie()) {
					Logger.out.log(CATEGORY, "Talked to plushie.");
				}
			}
			Logger.out.log(CATEGORY, "Done.");
			DailyLog.props.addBoolean(CATEGORY, "done", true);
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
		return helper.get("/faerieland/tdmbgpop.phtml")
				.send();
	}
	
	private boolean talkToThePlushie() throws HttpHelperException {
		return !DomUtils.getContent(Jsoup.parse(loadTalk().getContent().get()))
				.text().contains("You have already visited the plushie today");
	}
	
	private HttpResponse loadTalk() throws HttpHelperException {
		return helper.post("/faerieland/tdmbgpop.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/faerieland/tdmbgpop.phtml"))
				.addFormParameter("talkto", 1)
				.send();
	}
}

