package com.neopetsconnect.dailies;

import org.jsoup.Jsoup;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;

public class HealingSprings implements ConfigProperties {

	private static final String CATEGORY = HEALING_SPRINGS;
	
	private final HttpHelper helper;
	
	public HealingSprings(HttpHelper helper) {
		this.helper = helper;
	}
	
	public int call() {
		try {
			parseMain();
			if(heal()) {
				Logger.out.log(CATEGORY, "Healed :)");
			} else {
				Logger.out.log(CATEGORY, "Have to wait :(");
			}
		} catch (HttpHelperException e) {
			throw new RuntimeException("Something when wrong.", e);
		}
		return HEALING_SPRINGS_FREQ;
	}

	private boolean parseMain() throws HttpHelperException {
		loadMain();
		return true;
	}
	
	private HttpResponse loadMain() throws HttpHelperException {
		return helper.get("/faerieland/springs.phtml")
				.send();
	}
	
	private boolean heal() throws HttpHelperException {
		return !DomUtils.getContent(Jsoup.parse(loadHeal().getContent().get()))
				.text().contains("Sorry! - My magic is not fully restored yet. Please try back later.");
	}
	
	private HttpResponse loadHeal() throws HttpHelperException {
		return helper.post("/faerieland/springs.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/faerieland/springs.phtml"))
				.addFormParameter("type", "heal")
				.send();
	}
}

