package com.neopetsconnect.dailies;

import org.jsoup.nodes.Element;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;

public class TDMGPoP implements Categories {

  private static final String CATEGORY = TDMGPOP;
  private final HttpHelper helper;

  public TDMGPoP(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    if (!ConfigProperties.isTdmgpopEnabled()) {
      return ConfigProperties.getDailiesRefreshFreq();
    }
    if (!DailyLog.props().getBoolean(CATEGORY, "done", false)) {
      parseMain();
      if (talkToThePlushie()) {
        Logger.out.log(CATEGORY, "Talked to plushie.");
      }
    }
    Logger.out.log(CATEGORY, "Done.");
    DailyLog.props().addBoolean(CATEGORY, "done", true);
    return ConfigProperties.getDailiesRefreshFreq();
  }

  private boolean parseMain() {
    loadMain();
    return true;
  }

  private HttpResponse loadMain() {
    return helper.get("/faerieland/tdmbgpop.phtml").send();
  }

  private boolean talkToThePlushie() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(talkRequest());
      return !content.text().contains("You have already visited the plushie today");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpRequest talkRequest() {
    return helper.post("/faerieland/tdmbgpop.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/faerieland/tdmbgpop.phtml"))
        .addFormParameter("talkto", 1);
  }
}

