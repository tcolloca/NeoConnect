package com.neopetsconnect.dailies;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.Logger;

public class LabRay implements Categories {

  private static final String CATEGORY = LAB_RAY;
  private final HttpHelper helper;

  public LabRay(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    if (!ConfigProperties.isLabRayEnabled()) {
      return ConfigProperties.getDailiesRefreshFreq();
    }
    if (!DailyLog.props().getBoolean(CATEGORY, "done", false)) {
      loadMain();
      loadEnter();
      loadZap();
      DailyLog.props().addBoolean(CATEGORY, "done", true);
    }
    Logger.out.log(CATEGORY, "Done.");
    return ConfigProperties.getDailiesRefreshFreq();
  }

  private HttpResponse loadZap() {
    return helper.post("/process_lab2.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/lab2.phtml"))
        .addFormParameter("chosen", ConfigProperties.getLabRayPetName()).send();
  }

  private HttpResponse loadEnter() {
    return helper.post("/lab2.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/lab.phtml"))
        .addFormParameter("donation", 100).send();
  }

  private HttpResponse loadMain() {
    return helper.get("/lab.phtml").send();
  }
}
