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

public class ColtzanShrine implements ConfigProperties {

  private static final String CATEGORY = COLTZANS_SHRINE;

  private final HttpHelper helper;

  public ColtzanShrine(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
      parseMain();
      if (visit()) {
        Logger.out.log(CATEGORY, "Visited :)");
        DailyLog.props.addBoolean(CATEGORY, "done", true);
      } else {
        Logger.out.log(CATEGORY, "Have to wait :(");
      }
    } else {
      Logger.out.log(CATEGORY, "Done.");
    }
    return DAILIES_REFRESH_FREQ;
  }

  private boolean parseMain() {
    loadMain();
    return true;
  }

  private HttpResponse loadMain() {
    return helper.get("/desert/shrine.phtml").send();
  }

  private boolean visit() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(visitRequest());
      return !content.text()
          .contains("Maybe you should wait a while before visiting the shrine again....");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpRequest visitRequest() {
    return helper.post("/desert/shrine.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/desert/shrine.phtml"))
        .addFormParameter("type", "approach");
  }
}

