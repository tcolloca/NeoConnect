package com.neopetsconnect.dailies;

import org.jsoup.nodes.Element;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;

public class HealingSprings implements ConfigProperties {

  private static final String CATEGORY = HEALING_SPRINGS;

  private final HttpHelper helper;

  public HealingSprings(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    parseMain();
    if (heal()) {
      Logger.out.log(CATEGORY, "Healed :)");
    } else {
      Logger.out.log(CATEGORY, "Have to wait :(");
    }
    return HEALING_SPRINGS_FREQ;
  }

  private boolean parseMain() {
    loadMain();
    return true;
  }

  private HttpResponse loadMain() {
    return helper.get("/faerieland/springs.phtml").send();
  }

  private boolean heal() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(healRequest());
      return !content.text()
          .contains("Sorry! - My magic is not fully restored yet. Please try back later.");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpRequest healRequest() {
    return helper.post("/faerieland/springs.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/faerieland/springs.phtml"))
        .addFormParameter("type", "heal");
  }
}

