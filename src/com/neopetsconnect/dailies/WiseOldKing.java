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

public class WiseOldKing implements ConfigProperties {

  private static final String CATEGORY = WISE_OLD_KING;

  private final HttpHelper helper;

  public WiseOldKing(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
      if (parseMain()) {
        loadPhrase();
        DailyLog.props.addBoolean(CATEGORY, "done", true);
      }
    }
    Logger.out.log(CATEGORY, "Done.");
    return DAILIES_REFRESH_FREQ;
  }

  private boolean parseMain() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(mainRequest());
      return content.text().contains("Your Words of Wisdom");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpResponse loadPhrase() {
    HttpRequest req = helper.post("/medieval/process_wiseking.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/medieval/wiseking.phtml"));
    for (int i = 1; i <= 6; i++) {
      req.addFormParameter("qp" + i, "");
    }
    req.addFormParameter("qp7", "Abominable Snowball");
    return req.send();
  }

  private HttpRequest mainRequest() {
    return helper.get("/medieval/wiseking.phtml");
  }
}
