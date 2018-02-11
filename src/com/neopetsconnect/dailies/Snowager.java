package com.neopetsconnect.dailies;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpResponse;
import com.logger.main.TimeUnits;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class Snowager implements ConfigProperties {

  private static final String CATEGORY = SNOWAGER;
  private final HttpHelper helper;

  public Snowager(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    LocalDateTime now = Utils.neopetsNow();
    LocalDateTime nextTime = getNextAwakeTime(now);
    Logger.out.log(CATEGORY, "Next time: " + nextTime);
    Logger.out.log(CATEGORY, "Now: " + now);
    if (nextTime.getHour() == now.getHour()) {
      visit();
      Logger.out.log(CATEGORY, "Visited: " + now);
      nextTime = getNextAwakeTime(now.plusHours(1));
    }
    int returnTime =
        (int) (nextTime.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC));
    Logger.out.logTime(CATEGORY, "Return in: %.2f hours.", returnTime, TimeUnits.SECONDS,
        TimeUnits.HOURS);
    return returnTime + 60 * 5;
  }

  private LocalDateTime getNextAwakeTime(LocalDateTime nowTime) {
    int[] hours = {6, 14, 22};
    List<LocalDateTime> dateTimes = Arrays.stream(hours)
        .mapToObj(hour -> LocalDateTime.of(Utils.neopetsNow().toLocalDate(), LocalTime.of(hour, 0)))
        .collect(Collectors.toList());
    for (LocalDateTime dateTime : dateTimes) {
      if (nowTime.getHour() <= dateTime.getHour()) {
        return dateTime;
      }
    }
    return dateTimes.get(0).plusDays(1);
  }

  private void visit() {
    botherSnowager();
  }

  private void botherSnowager() {
    loadMain();
    loadSnowagerCave();
  }

  private HttpResponse loadSnowagerCave() {
    return helper.get("winter/snowager2.phtml")
        .addHeader(Headers.referer("http://www.neopets.com/winter/snowager.phtml")).send();
  }

  private HttpResponse loadMain() {
    return helper.get("/winter/snowager.phtml").send();
  }
}
