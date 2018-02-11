package com.neopetsconnect.dailies;

import org.jsoup.nodes.Element;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;

public class YeOldeFishingVortex implements ConfigProperties {

  private static final String CATEGORY = FISHING_VORTEX;

  private final HttpHelper helper;

  public YeOldeFishingVortex(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    if (!DailyLog.props.getBoolean(FISHING_VORTEX, "done", false)) {
      parseMain();
      if (fish()) {
        Logger.out.log(CATEGORY, "Fished :)");
        DailyLog.props.addBoolean(FISHING_VORTEX, "done", true);
      } else {
        Logger.out.log(CATEGORY, "Have to wait :(");
      }
    } else {
      Logger.out.log(CATEGORY, "Done.");
    }
    return DAILIES_REFRESH_FREQ;
  }

  private boolean fish() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(fishingRequest());
      return !content.text().contains("patient");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private boolean parseMain() {
    loadMain();
    return true;
  }

  private HttpRequest fishingRequest() {
    return helper.post("/water/fishing.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/water/fishing.phtml"))
        .addFormParameter("go_fish", 1);
  }

  private HttpResponse loadMain() {
    return helper.get("/water/fishing.phtml").send();
  }
}

