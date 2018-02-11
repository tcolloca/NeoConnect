package com.neopetsconnect.dailies;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;

public class ForgottenShore implements ConfigProperties {


  private static final String CATEGORY = FORGOTTEN_SHORE;

  private final HttpHelper helper;

  public ForgottenShore(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
      retrieve();
      Logger.out.log(CATEGORY, "Retrieved :)");
      DailyLog.props.addBoolean(CATEGORY, "done", true);
    }
    Logger.out.log(CATEGORY, "Done.");
    return DAILIES_REFRESH_FREQ;
  }

  private void retrieve() {
    String link = parseMain();
    if (link != null) {
      loadRetrieve(link);
    } else {
      Logger.out.log(CATEGORY, "Nothing to be found :/");
    }
  }

  private String parseMain() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Document doc = httpUtils.getDocument(mainRequest());
      Elements clickable = doc.getElementById("shore_back").select(":root > a");
      if (clickable.isEmpty()) {
        return null;
      }
      return clickable.attr("href");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpResponse loadRetrieve(String link) {
    return helper.get("/pirates/forgottenshore.phtml" + link)
        .addHeader(Headers.referer("http://www.neopets.com/pirates/forgottenshore.phtml")).send();
  }

  private HttpRequest mainRequest() {
    return helper.get("/pirates/forgottenshore.phtml");
  }
}

