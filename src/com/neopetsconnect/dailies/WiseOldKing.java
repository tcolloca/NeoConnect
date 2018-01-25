package com.neopetsconnect.dailies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;

public class WiseOldKing implements ConfigProperties {

	private static final String CATEGORY = WISE_OLD_KING;

	private final HttpHelper helper;
	
	public WiseOldKing(HttpHelper helper) {
		this.helper = helper;
	}
	
	public int call() {
		try {
			if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
				if (parseMain()) {
					loadPhrase();
					DailyLog.props.addBoolean(CATEGORY, "done", true);
				}
			}
			Logger.out.log(CATEGORY, "Done.");
		} catch (HttpHelperException e) {
			throw new RuntimeException("Something when wrong.", e);
		}
		return DAILIES_REFRESH_FREQ;
	}

	private boolean parseMain() throws HttpHelperException {
		String content = loadMain().getContent().get();
		Document doc = Jsoup.parse(content);
		return DomUtils.getContent(doc).text().contains("Your Words of Wisdom");
	}
	
	private HttpResponse loadPhrase() throws HttpHelperException {
		HttpRequest req = helper.post("/medieval/process_wiseking.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/medieval/wiseking.phtml"));
		for (int i =  1; i <= 6; i++) {
			req.addFormParameter("qp" + i, "");
		}
		req.addFormParameter("qp7", "Abominable Snowball");
		return req.send();
	}
	
	private HttpResponse loadMain() throws HttpHelperException {
		return helper.get("/medieval/wiseking.phtml")
				.send();
	}
}
