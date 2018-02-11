package com.neopetsconnect.dailies.trainingschool;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.common.base.Strings;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.logger.main.TimeUnits;
import com.neopetsconnect.dailies.status.CompletedStatus;
import com.neopetsconnect.dailies.status.NoneStatus;
import com.neopetsconnect.dailies.status.PendingPayStatus;
import com.neopetsconnect.dailies.status.Status;
import com.neopetsconnect.dailies.status.WaitingStatus;
import com.neopetsconnect.main.Item;
import com.neopetsconnect.main.Main;
import com.neopetsconnect.shopwizard.ShopWizard;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class TrainingSchool implements ConfigProperties {

  private static final String CATEGORY = TRAINING_SCHOOL;

  private static final int[] STATS_TIERS =
      {7, 12, 19, 34, 54, 84, 124, 199, 249, 349, 399, 449, 499, 549, 599, 649, 699, 749};

  private static final int[] SCHOOL_TIERS = {20, 40, 80, 100, 120, 150, 200, 250};

  private static final int LEVEL_TIER_OFFSET = 5;

  private final HttpHelper helper;
  private final ShopWizard shopWizard;

  public TrainingSchool(HttpHelper helper) {
    this.helper = helper;
    this.shopWizard = new ShopWizard(helper);
  }

  public static void main(String[] args) {
    HttpHelper helper = Main.initHelper();
    Main.handleSession(helper);

    new TrainingSchool(helper).call();
  }

  public int call() {
    Status status;
    Status prev = null;
    while (!((status = getStatus()) instanceof WaitingStatus)) {
      Logger.out.log(CATEGORY, status.getClass().getSimpleName());
      if (status instanceof WaitingStatus) {
        Logger.out.log(CATEGORY, status);
        return ((WaitingStatus) status).getWaitTime().toSecondOfDay();
      }
      if (status instanceof CompletedStatus) {
        StatIncrease statIncrease = parseComplete();
        if (statIncrease != null) {
          Logger.out.log(CATEGORY, statIncrease);
        }
      }
      if (status instanceof NoneStatus) {
        String statType = STAT_TYPE;
        if (Strings.isNullOrEmpty(STAT_TYPE)) {
          statType = getStatToTrain();
        }
        train(statType);
        Logger.out.log(CATEGORY, "Started course for " + statType);
      }
      if (status instanceof PendingPayStatus) {
        pay((PendingPayStatus) status);
      }
      if (status.equals(prev)) {
        throw new RuntimeException("Looping at training school");
      }
      prev = status;
    }
    Logger.out.logTime(CATEGORY, "Have to wait %.0f minutes.",
        ((WaitingStatus) status).getWaitTime().toSecondOfDay() + 60 * 5, TimeUnits.SECONDS,
        TimeUnits.MINUTES);
    return ((WaitingStatus) status).getWaitTime().toSecondOfDay() + 60 * 5;
  }

  private String getStatToTrain() {
    Stats stats = getStats();
    if (stats.getLevel() > 250) {
      throw new IllegalStateException("Can't train in Training school!!!!");
    }
    int maxStat = Math.max(stats.getHealth(), Math.max(stats.getDefence(), stats.getStrength()));
    // If a stat is over 2 times level, train level.
    if (maxStat > 2 * stats.getLevel()) {
      return "level";
    }
    int healthTier = getStatTier(stats.getHealth());
    int defenceTier = getStatTier(stats.getDefence());
    int strengthTier = getStatTier(stats.getStrength());
    int minTier = Math.min(healthTier, Math.min(defenceTier, strengthTier));
    // If the next tier for a stat is over 2 times the current level, probably level should be
    // trained.
    if (stats.getLevel() < Math.ceil(minTier / 2)) {
      int levelTier = (int) Math.ceil(minTier / 2.0);
      int currentTier = getLevelTier(stats.getLevel());
      // If we are above the target tier, and we are not getting close to tier end, train level.
      if (currentTier >= levelTier && stats.getLevel() < levelTier - LEVEL_TIER_OFFSET) {
        return "level";
      } else {
        // If not, train evenly min stat.
        int minStat =
            Math.min(stats.getHealth(), Math.min(stats.getDefence(), stats.getStrength()));
        if (minStat == stats.getHealth()) {
          return "endurance";
        }
        if (minStat == stats.getStrength()) {
          return "strength";
        }
        if (minStat == stats.getDefence()) {
          return "defence";
        }
      }
    }
    // If level is enough for next stat tier, train greedily stat to its next tier.
    if (minTier == healthTier) {
      return "endurance";
    }
    if (minTier == strengthTier) {
      return "strength";
    }
    if (minTier == defenceTier) {
      return "defence";
    }
    throw new IllegalStateException("Unreachable code.");
  }

  private int getStatTier(int stat) {
    return getTier(stat, STATS_TIERS);
  }

  private int getLevelTier(int level) {
    return getTier(level, SCHOOL_TIERS);
  }

  private int getTier(int value, int[] tiers) {
    int nextTier = tiers[0];
    for (int i = 1; value > tiers[i - 1]; i++) {
      nextTier = tiers[i];
    }
    return nextTier;
  }

  private void pay(PendingPayStatus status) {
    List<Item> codestones = ((PendingPayStatus) status).getItems();
    Logger.out.log(CATEGORY,
        "Needed: " + codestones.stream().map(it -> it.getName()).collect(Collectors.toList()));
    if (shopWizard.buyItems(codestones, SHOP_WIZ_SEARCH_TIMES).size() == codestones.size()) {
      Logger.out.log(CATEGORY, "Bought all.");
      if (pay()) {
        Logger.out.log(CATEGORY, "Started training.");
      }
    }
  }

  private Status getStatus() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(statusRequest());
      Elements pets = content.select(":root > div").get(1).select(":root > table > tbody > tr");
      int index = 6;
      for (int i = 0; i < pets.size(); i += 2) {
        if (pets.get(i).select(":root > td").text().contains("jimtry")) {
          index = i;
          break;
        }
      }
      Element statusCol = pets.get(index + 1).select(":root > td").get(1);
      String status = statusCol.text().trim();
      if (status.equals("Course Finished!")) {
        return new CompletedStatus();
      } else if (status.isEmpty()) {
        return new NoneStatus();
      } else if (status.contains("This course has not been paid for yet")) {
        List<Item> codestones = statusCol.select(":root > p > b").stream()
            .map(elem -> new Item(elem.text().trim())).collect(Collectors.toList());
        return new PendingPayStatus(codestones);
      } else {
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("H 'hrs', m 'minutes', s 'seconds'");
        TemporalAccessor time = formatter.parse(status.split(":")[1].trim());
        return new WaitingStatus(Utils.toLocalTime(time));
      }
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private StatIncrease parseComplete() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Document doc = httpUtils.getDocument(completeRequest());
      String text = doc.text();
      String statTypeStr = text.split("now has increased ")[1].split("!!!")[0].trim();
      int amount =
          text.contains("SUPER BONUS") ? Integer.parseInt(Utils.between(text, "up", "points")) : 1;
      return new StatIncrease(StatType.fromName(statTypeStr), amount);
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private boolean train(String statType) {
    loadCourses();
    return loadTrain(statType).getStatusCode() == 200;
  }

  private boolean pay() {
    return loadPay().getStatusCode() == 200;
  }

  private Stats getStats() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Document doc = httpUtils.getDocument(quickRefRequest());
      String petName = "jimtry";
      Element petElement = doc.getElementById(petName + "_details");
      if (petElement == null) {
        throw new IllegalStateException("Current pet should be: " + petName);
      }
      List<String> statsRows = petElement.select(":root > table > tbody > tr").get(1)
          .select(":root > td > div").first().select(":root > table > tbody > tr").stream()
          .map(elem -> elem.select(":root > td").text()).collect(Collectors.toList());
      return Stats.newBuilder().setLevel(Integer.parseInt(statsRows.get(4).trim()))
          .setHealth(Integer.parseInt(statsRows.get(5).split("/")[1].trim()))
          .setStrength(Integer.parseInt(statsRows.get(8).replaceAll("[a-zA-Z() ]", "")))
          .setDefence(Integer.parseInt(statsRows.get(9).replaceAll("[a-zA-Z() ]", "")))
          .setMove(Integer.parseInt(statsRows.get(10).replaceAll("[a-zA-Z() ]", ""))).build();
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpResponse loadTrain(String statType) {
    return helper.post("/island/process_training.phtml")
        .addHeader(Headers.referer("http://www.neopets.com/island/training.phtml?type=courses"))
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addFormParameter("type", "start").addFormParameter("pet_name", "jimtry")
        .addFormParameter("course_type",
            Character.toUpperCase(statType.charAt(0)) + statType.toLowerCase().substring(1))
        .send();
  }

  private HttpResponse loadPay() {
    return helper.get("/island/process_training.phtml")
        .addHeader(Headers.referer("http://www.neopets.com/island/training.phtml?type=status"))
        .addQueryParameter("type", "pay").addQueryParameter("pet_name", "jimtry").send();
  }

  private HttpRequest completeRequest() {
    return helper.post("/island/process_training.phtml")
        .addHeader(Headers.referer("http://www.neopets.com/island/training.phtml?type=status"))
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addFormParameter("type", "complete").addFormParameter("pet_name", "jimtry");
  }

  private HttpResponse loadCourses() {
    return helper.get("/island/training.phtml").addQueryParameter("type", "courses").send();
  }

  private HttpRequest statusRequest() {
    return helper.get("/island/training.phtml").addQueryParameter("type", "status");
  }

  private HttpRequest quickRefRequest() {
    return helper.get("/quickref.phtml");
  }
}
