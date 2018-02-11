package com.neopetsconnect.dailies;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;

public class FruitMachine implements ConfigProperties {

  private static final String CATEGORY = FRUIT_MACHINE;

  private final HttpHelper helper;

  public FruitMachine(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
      String ck = parseMain();
      if (ck != null) {
        if (spin(ck)) {
          Logger.out.log(CATEGORY, "Spinned fruit machine.");
        }
      }
    }
    Logger.out.log(CATEGORY, "Done.");
    DailyLog.props.addBoolean(CATEGORY, "done", true);
    return DAILIES_REFRESH_FREQ;
  }

  private String parseMain() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(mainRequest());
      Elements inputs = content.select(":root > div.result").select(":root > form > input");
      if (inputs.isEmpty()) {
        return null;
      }
      return inputs.get(1).attr("value");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpRequest mainRequest() {
    return helper.get("/desert/fruit/index.phtml");
  }

  private boolean spin(String ck) {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(spinRequest(ck));
      return !content.text().contains("You have already spinned the fruit machine today.");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpRequest spinRequest(String ck) {
    return helper.post("/desert/fruit/index.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/desert/fruit/index.phtml"))
        .addFormParameter("spin", 1).addFormParameter("ck", ck);
  }
}
