package com.neopetsconnect.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import com.logger.main.TimeUnits;

public class Utils {

  public static int getNps(String npsText) {
    return Integer.parseInt(npsText.replace("NP", "").replace("np", "").replace(",", "").trim());
  }

  public static void sleep(double secs) {
    try {
      Thread.sleep((long) (secs * 1000));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void sleepAndLog(String category, double secs) {
    sleepAndLog(category, secs, TimeUnits.SECONDS);
  }

  public static void sleepAndLog(String category, double secs, TimeUnits dstUnits) {
    try {
      Logger.out.logTime(category, "Sleeping %.3f " + dstUnits.name().toLowerCase() + ".", secs,
          TimeUnits.SECONDS, dstUnits);
      Thread.sleep((long) (secs * 1000));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> List<T> merge(Collection<List<T>> collection) {
    return collection.stream().flatMap(Collection::stream).collect(Collectors.toList());
  }

  public static String stackTraceToString(Throwable th) {
    Throwable toPrintTh = th;
    Throwable prev = null;
    String message = "";
    while (toPrintTh != null && !toPrintTh.equals(prev)) {
      message += "Cause:\r\n" + toPrintTh.toString() + "\r\n" + String.join("\r\n", Arrays
          .stream(toPrintTh.getStackTrace()).map(st -> st.toString()).collect(Collectors.toList()));
      prev = toPrintTh;
      if (toPrintTh instanceof ExceptionInInitializerError) {
        toPrintTh = ((ExceptionInInitializerError) toPrintTh).getException();
      } else {
        toPrintTh = th.getCause();
      }
    }
    return message;
  }

  public static String between(String text, String start, String end) {
    return text.split(start)[1].split(end)[0].trim();
  }

  public static LocalDateTime neopetsNow() {
    return LocalDateTime.now(ZoneId.of("US/Pacific"));
  }

  public static LocalTime toLocalTime(TemporalAccessor time) {
    int hours = time.get(ChronoField.HOUR_OF_DAY);
    int mins = time.get(ChronoField.MINUTE_OF_HOUR);
    int secs = time.get(ChronoField.SECOND_OF_MINUTE);
    return LocalTime.of(hours, mins, secs);
  }

  public static String maxLength(String s, int n) {
    if (s.length() <= n) {
      return s;
    }
    return s.substring(0, Math.min(s.length(), n - 10)) + "\r\n...";
  }

  public static String maxLengthReversed(String s, int n) {
    if (s.length() <= n) {
      return s;
    }
    return "...\r\n" + s.substring(Math.max(s.length() - n + 10, 0), s.length());
  }

  public static double random(double min, double max) {
    return new Random().nextDouble() * (max - min) + min;
  }

  public static double random(int min, int max) {
    return new Random().nextInt(max - min) + min;
  }
}
