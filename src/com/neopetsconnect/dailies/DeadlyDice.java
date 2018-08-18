package com.neopetsconnect.dailies;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Random;

import org.jsoup.nodes.Element;

import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.logger.main.TimeUnits;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class DeadlyDice implements Categories {

  private static final String CATEGORY = DEADLY_DICE;

  private final HttpHelper helper;

  public DeadlyDice(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    LocalDateTime now = Utils.neopetsNow();
    Logger.out.log(CATEGORY, "Now: " + now);
    if (now.getHour() != 0) {
      LocalDateTime nextTime = LocalDateTime.of(now.toLocalDate().plusDays(1), 
          LocalTime.of(0, new Random().nextInt(55)));
      Logger.out.log(CATEGORY, "Next time: " + nextTime);
      int returnTime =
          (int) (nextTime.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC));
      Logger.out.logTime(CATEGORY, "Return in: %.2f hours.", returnTime, TimeUnits.SECONDS,
          TimeUnits.HOURS);
      return returnTime;
    }
    
    if (!ConfigProperties.isDeadlyDiceEnabled()) {
      return ConfigProperties.getDailiesRefreshFreq();
    }
    if (!DailyLog.props().getBoolean(CATEGORY, "done", false)) {
      boolean aux = HttpHelper.log;
      HttpHelper.log = true;
      if (loadMain()) {

    	  // TODO: Do something.
    	}
      HttpHelper.log = aux;
    }
//    Logger.out.log(CATEGORY, "Done.");
    DailyLog.props().addBoolean(CATEGORY, "done", true);
    return ConfigProperties.getDailiesRefreshFreq();
  }

  private boolean loadMain() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(mainRequest());
      return !content.text().contains("The Count is sleeping");
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpRequest mainRequest() {
    return helper.get("/worlds/deadlydice.phtml");
  }

//  private boolean collectInterest() {
//    HttpUtils httpUtils = HttpUtils.newInstance();
//    try {
//      Element content = httpUtils.getContent(collectInterestRequest());
//      return content.text().contains("You have already collected your interest today");
//    } catch (Throwable th) {
//      httpUtils.logRequestResponse();
//      throw th;
//    }
//  }
//
//  private HttpRequest collectInterestRequest() {
//    return helper.post("/process_bank.phtml")
//        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
//        .addHeader(Headers.referer("http://www.neopets.com/bank.phtml"))
//        .addFormParameter("type", "interest");
//  }
}
