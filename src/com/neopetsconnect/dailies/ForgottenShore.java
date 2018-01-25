package com.neopetsconnect.dailies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.Logger;

public class ForgottenShore implements ConfigProperties {

	private static final String CATEGORY = FORGOTTEN_SHORE;
	
	private final HttpHelper helper;
	
	public ForgottenShore(HttpHelper helper) {
		this.helper = helper;
	}
	
	public int call() {
		try {
			if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
				retrieve();
				Logger.out.log(CATEGORY, "Retrieved :)");
				DailyLog.props.addBoolean(CATEGORY, "done", true);
			}
			Logger.out.log(CATEGORY, "Done.");
		} catch (HttpHelperException e) {
			throw new RuntimeException("Something when wrong.", e);
		}
		return DAILIES_REFRESH_FREQ;
	}
	
	private void retrieve() throws HttpHelperException {
		String link = parseMain();
		if (link != null) {
			loadRetrieve(link);
		} else {
			Logger.out.log(CATEGORY, "Nothing to be found :/");
		}
	}

	private String parseMain() throws HttpHelperException {
		String content = loadMain().getContent().get();
		Document doc = Jsoup.parse(content);
		Elements clickable = doc.getElementById("shore_back").select(":root > a");
		if (clickable.isEmpty()) {
			return null;
		}
		return clickable.attr("href");
	}
	
	private HttpResponse loadRetrieve(String link) throws HttpHelperException {
		return helper.get("/pirates/forgottenshore.phtml" + link)
				.addHeader(Headers.referer("http://www.neopets.com/pirates/forgottenshore.phtml"))
				.send();
	}
	
	private HttpResponse loadMain() throws HttpHelperException {
		return helper.get("/pirates/forgottenshore.phtml")
				.send();
	}
}

