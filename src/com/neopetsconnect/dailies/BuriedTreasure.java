package com.neopetsconnect.dailies;

import org.jsoup.nodes.Element;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.logger.main.TimeUnits;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class BuriedTreasure implements Categories {

  private static final String CATEGORY = BURIED_TREASURE;

  private final HttpHelper helper;

  public BuriedTreasure(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    if (!ConfigProperties.isBuriedTreasureEnabled()) {
      return ConfigProperties.getDailiesRefreshFreq();
    }
    parseMain();
    int waitTime = parseBuriedTreasure();
    if (waitTime == 0) {
      int x = ConfigProperties.getBuriedTreasureX();
      int y = ConfigProperties.getBuriedTreasureY();
      play(x, y);
      Logger.out.log(CATEGORY, String.format("Played (%d, %d).", x, y));
    } else {
      Logger.out.logTime(CATEGORY, "Have to wait %.0f minutes.", waitTime, TimeUnits.SECONDS,
          TimeUnits.MINUTES);
      return waitTime;
    }
    return ConfigProperties.getDailiesRefreshFreq();
  }

  private boolean parseMain() {
    loadMain();
    return true;
  }

  private int parseBuriedTreasure() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(buriedTreasureRequest());
      String text = content.select(":root > div > center > p > b").text();
      if (text.trim().isEmpty()) {
        return 0;
      }
      return Integer.parseInt(Utils.between(text, "another", "minutes")) * 60;
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private boolean play(int x, int y) {
    return loadPlay(x, y).getStatusCode() == 200;
  }

  private HttpResponse loadMain() {
    return helper.get("/pirates/buriedtreasure/index.phtml").send();
  }

  private HttpRequest buriedTreasureRequest() {
    return helper.get("/pirates/buriedtreasure/buriedtreasure.phtml?")
        .addHeader(Headers.referer("http://www.neopets.com/pirates/buriedtreasure/index.phtml"));
  }

  private HttpResponse loadPlay(int x, int y) {
    return helper.get("/pirates/buriedtreasure/buriedtreasure.phtml?" + x + "," + y)
        .addHeader(
            Headers.referer("http://www.neopets.com/pirates/buriedtreasure/buriedtreasure.phtml?"))
        .send();
  }
}
