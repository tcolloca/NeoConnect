package com.neopetsconnect.dailies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;

public class FruitMachine implements ConfigProperties {

	private static final String CATEGORY = FRUIT_MACHINE;

	private final HttpHelper helper;
	
	public FruitMachine(HttpHelper helper) {
		this.helper = helper;
	}
	
	public int call() {
		try {
			if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
				String ck = parseMain();
				if (ck != null) {
					if(spin(ck)) {
						Logger.out.log(CATEGORY, "Spinned fruit machine.");
					}
				}
			}
			Logger.out.log(CATEGORY, "Done.");
			DailyLog.props.addBoolean(CATEGORY, "done", true);
		} catch (HttpHelperException e) {
			throw new RuntimeException("Something when wrong.", e);
		}
		return DAILIES_REFRESH_FREQ;
	}

	private String parseMain() throws HttpHelperException {
		String content = loadMain().getContent().get();
		Document doc = Jsoup.parse(content);
		Elements inputs = DomUtils.getContent(doc).select(":root > div.result")
			.select(":root > form > input");
		if (inputs.isEmpty()) {
			return null;
		}
		return inputs.get(1).attr("value");
	}
	
	private HttpResponse loadMain() throws HttpHelperException {
		return helper.get("/desert/fruit/index.phtml")
				.send();
	}
	
	private boolean spin(String ck) throws HttpHelperException {
		return !DomUtils.getContent(Jsoup.parse(loadSpin(ck).getContent().get()))
				.text().contains("You have already spinned the fruit machine today.");
	}
	
	private HttpResponse loadSpin(String ck) throws HttpHelperException {
		return helper.post("/desert/fruit/index.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/desert/fruit/index.phtml"))
				.addFormParameter("spin", 1)
				.addFormParameter("ck", ck)
				.send();
	}
}
