package com.neopetsconnect.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.neopetsconnect.dailies.trainingschool.Stats;
import com.neopetsconnect.exceptions.FaerieQuestException;
import com.neopetsconnect.exceptions.ShopWizardBannedException;
import com.neopetsconnect.utils.ConfigProperties;

public class DomTester implements ConfigProperties {

  private static final int[] STATS_TIERS =
      {7, 12, 19, 34, 54, 84, 124, 199, 249, 349, 399, 449, 499, 549, 599, 649, 699, 749};

  private static final int[] SCHOOL_TIERS = {20, 40, 80, 100, 120, 150, 200, 250};

  private static final int LEVEL_TIER_OFFSET = 5;

  public static void main(String[] args)
      throws IOException, ShopWizardBannedException, FaerieQuestException {
    Document doc = Jsoup.parse(new File("quickref.html"), "UTF-8");
    String petName = "jimtry";
    Element petElement = doc.getElementById(petName + "_details");
    if (petElement == null) {
      throw new IllegalStateException("Current pet should be: " + petName);
    }
    List<String> statsRows = petElement.select(":root > table > tbody > tr").get(1)
        .select(":root > td > div").first().select(":root > table > tbody > tr").stream()
        .map(elem -> elem.select(":root > td").text()).collect(Collectors.toList());
    Stats stats = Stats.newBuilder().setLevel(95).setHealth(161).setStrength(160).setDefence(160)
        .setMove(Integer.parseInt(statsRows.get(10).replaceAll("[a-zA-Z() ]", ""))).build();
    System.out.println(stats);
    if (stats.getLevel() > 250) {
      throw new IllegalStateException("Can't train in Training school!!!!");
    }
    int maxStat = Math.max(stats.getHealth(), Math.max(stats.getDefence(), stats.getStrength()));
    if (maxStat > 2 * stats.getLevel()) {
      System.out.println("level");
      return;
    }
    int healthTier = getStatTier(stats.getHealth());
    int defenceTier = getStatTier(stats.getDefence());
    int strengthTier = getStatTier(stats.getStrength());
    int minTier = Math.min(healthTier, Math.min(defenceTier, strengthTier));
    System.out.println(minTier);
    if (stats.getLevel() < Math.ceil(minTier / 2)) {
      int levelTier = (int) Math.ceil(minTier / 2.0);
      int currentTier = getLevelTier(stats.getLevel());
      System.out.println("curr tier " + currentTier);
      if (currentTier >= levelTier && stats.getLevel() < levelTier - LEVEL_TIER_OFFSET) {
        System.out.println("level");
        return;
      } else {
        System.out.println("level to " + Math.ceil(minTier / 2.0));
        int minStat =
            Math.min(stats.getHealth(), Math.min(stats.getDefence(), stats.getStrength()));
        if (minStat == stats.getHealth()) {
          System.out.println("endurance");
          return;
        }
        if (minStat == stats.getStrength()) {
          System.out.println("strength");
          return;
        }
        if (minStat == stats.getDefence()) {
          System.out.println("defence");
          return;
        }
        throw new IllegalStateException("Unreachable code.");
      }
    }
    if (minTier == healthTier) {
      System.out.println("endurance");
      return;
    }
    if (minTier == strengthTier) {
      System.out.println("strength");
      return;
    }
    if (minTier == defenceTier) {
      System.out.println("defence");
      return;
    }
    throw new IllegalStateException("Unreachable code.");
  }

  private static int getStatTier(int stat) {
    return getTier(stat, STATS_TIERS);
  }

  private static int getLevelTier(int level) {
    return getTier(level, SCHOOL_TIERS);
  }

  private static int getTier(int value, int[] tiers) {
    int nextTier = tiers[0];
    for (int i = 1; value > tiers[i - 1]; i++) {
      nextTier = tiers[i];
    }
    return nextTier;
  }
}
