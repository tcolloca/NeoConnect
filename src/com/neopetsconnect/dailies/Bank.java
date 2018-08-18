package com.neopetsconnect.dailies;

import org.jsoup.nodes.Element;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;

public class Bank implements Categories {

  private static final String CATEGORY = BANK;

  private final HttpHelper helper;

  public Bank(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    if (!ConfigProperties.isBankEnabled()) {
      return ConfigProperties.getDailiesRefreshFreq();
    }
    if (!DailyLog.props().getBoolean(CATEGORY, "done", false)) {
    	if (loadMain()) {
        if (collectInterest()) {
          Logger.out.log(CATEGORY, "Collected interest.");
        }	
    	}
    }
    Logger.out.log(CATEGORY, "Done.");
    DailyLog.props().addBoolean(CATEGORY, "done", true);
    return ConfigProperties.getDailiesRefreshFreq();
  }

  private boolean loadMain() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(mainRequest());
      return !content.text().contains("You have already collected your interest today");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpRequest mainRequest() {
    return helper.get("/bank.phtml");
  }

  private boolean collectInterest() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(collectInterestRequest());
      return content.text().contains("You have already collected your interest today");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpRequest collectInterestRequest() {
    return helper.post("/process_bank.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/bank.phtml"))
        .addFormParameter("type", "interest");
  }
}
