package com.neopetsconnect.utils;

import java.util.Arrays;
import java.util.List;
import com.neopetsconnect.main.Status;
import com.neopetsconnect.stores.NeopetsStoreId;

public class ConfigProperties implements Categories {

  public static List<String> listCategories() {
    return ConfigPropertiesReader.props.listCategories();
  }
  
  public static List<String> listSubCategories(String category) {
    return ConfigPropertiesReader.props.listSubCategories(category);
  }
  
  public static boolean contains(String category) {
    return ConfigPropertiesReader.props.contains(category);
  }
  
  public static boolean contains(String category, String subcategory) {
    return ConfigPropertiesReader.props.contains(category, subcategory);
  }
  
  public static <T> T get(String category, String subcategory, Class<T> clazz) {
    if (clazz.equals(Integer.class)) {
      return clazz.cast(ConfigPropertiesReader.props.getInt(category, subcategory));
    } else if (clazz.equals(Double.class)) {
      return clazz.cast(ConfigPropertiesReader.props.getDouble(category, subcategory));
    } else if (clazz.equals(String.class)) {
      return clazz.cast(ConfigPropertiesReader.props.getString(category, subcategory));
    } else if (clazz.equals(Boolean.class)) {
      return clazz.cast(ConfigPropertiesReader.props.getBoolean(category, subcategory));
    }
    throw new IllegalStateException("Unknown type: " + clazz);
  }
  
  public static <T> void set(String category, String subcategory, Class<T> clazz, T value) {
    if (clazz.equals(Integer.class)) {
      ConfigPropertiesReader.props.addInt(category, subcategory, (Integer) value);
    } else if (clazz.equals(Double.class)) {
      ConfigPropertiesReader.props.addDouble(category, subcategory, (Double) value);
    } else if (clazz.equals(String.class)) {
      ConfigPropertiesReader.props.addString(category, subcategory, (String) value);
    } else if (clazz.equals(Boolean.class)) {
      ConfigPropertiesReader.props.addBoolean(category, subcategory, (Boolean) value);
    } else {
      throw new IllegalStateException("Unknown type: " + clazz);
    }
  }
  
  // MAIN
  public static int getDailiesWaitSleep() {
    return ConfigPropertiesReader.props.getInt(DAILIES, "dailies_wait_sleep");
  }

  // SESSION
  public static String getUsername() {
    return ConfigPropertiesReader.props.getString(SESSION, "username");
  }
  public static String getPassword() {
    return ConfigPropertiesReader.props.getString(SESSION, "password");
  }
  public static String getPin() {
    return ConfigPropertiesReader.props.getString(SESSION, "pin");
  }

  // RESTOCK
  public static boolean isRestockEnabled() {
    return ConfigPropertiesReader.props.getBoolean(RESTOCK, "enabled");
  }
  public static String getStoreName() {
    return ConfigPropertiesReader.props.getString(RESTOCK, "store");
  }
  public static int getStoreId() {
    return NeopetsStoreId.getByName(getStoreName()).id;
  }
  public static int getMinProfit() {
    return ConfigPropertiesReader.props.getInt(RESTOCK, "min_profit");
  }
  public static int getMinStopProfit() {
    return ConfigPropertiesReader.props.getInt(RESTOCK, "min_stop_profit");
  }
  public static int getMaxRestockTime() {
    return ConfigPropertiesReader.props.getInt(RESTOCK, "max_restock_time");
  }
  public static int getMaxTimeSleep() {
    return ConfigPropertiesReader.props.getInt(RESTOCK, "max_time_sleep");
  }
  public static int getCheckBanTime() {
    return ConfigPropertiesReader.props.getInt(RESTOCK, "check_ban_time");
  }
  public static int getAfterBoughtSleepTime() {
    return ConfigPropertiesReader.props.getInt(RESTOCK, "after_bought_sleep_time");
  }
  public static double getMinRefreshTime() {
    return ConfigPropertiesReader.props.getDouble(RESTOCK, "min_refresh_time");
  }
  public static double getMaxRefreshTime() {
    return ConfigPropertiesReader.props.getDouble(RESTOCK, "max_refresh_time");
  }
  public static int getBanSleepTime() {
    return ConfigPropertiesReader.props.getInt(RESTOCK, "ban_sleep_time");
  }
  public static int getBanSleepFactor() {
    return ConfigPropertiesReader.props.getInt(RESTOCK, "ban_sleep_factor");
  }
  public static int getMinSoldOutWaitTime() {
    return ConfigPropertiesReader.props.getInt(RESTOCK, "min_sold_out_wait_time");
  }
  public static int getMaxSoldOutWaitTime() {
    return ConfigPropertiesReader.props.getInt(RESTOCK, "max_sold_out_wait_time");
  }
  
  // DAILIES
  public static String getDailiesLogPath() {
    return ConfigPropertiesReader.props.getString(DAILIES, "dailies_log_path");
  }
  public static String getDailiesLogFileFormat() {
    return "dd-MM-yyyy";
  }
  public static int getDailiesRefreshFreq() {
    return ConfigPropertiesReader.props.getInt(DAILIES, "refresh_freq");
  }
  
  // KITCHEN QUEST
  public static boolean isKitchenQuestEnabled() {
    return ConfigPropertiesReader.props.getBoolean(KITCHEN_QUEST, "enabled");
  }
  public static int getKitchenQuestDailyMaxSpend() {
    return ConfigPropertiesReader.props.getInt(KITCHEN_QUEST, "daily_max_spend");
  }
  public static int getKitchenQuestMaxCost() {
    return ConfigPropertiesReader.props.getInt(KITCHEN_QUEST, "max_cost");
  }

  // FOOD_CLUB
  public static boolean isFoodClubEnabled() {
    return ConfigPropertiesReader.props.getBoolean(FOOD_CLUB, "enabled");
  }

  // TRAINING_SCHOOL
  public static boolean isTrainingSchoolEnabled() {
    return ConfigPropertiesReader.props.getBoolean(TRAINING_SCHOOL, "enabled");
  }
  public static String getStatType() {
    return ConfigPropertiesReader.props.getString(TRAINING_SCHOOL, "stat_type", null);
  }

  // TDMGPOP
  public static boolean isTdmgpopEnabled() {
    return ConfigPropertiesReader.props.getBoolean(TDMGPOP, "enabled");
  }
   
  // HEALING_SPRINGS
  public static boolean isHealingSpringsEnabled() {
    return ConfigPropertiesReader.props.getBoolean(HEALING_SPRINGS, "enabled");
  }
  public static int getHealingSpringsFreq() {
    return ConfigPropertiesReader.props.getInt(HEALING_SPRINGS, "frequency");
  }

  // FRUIT_MACHINE
  public static boolean isFruitMachineEnabled() {
    return ConfigPropertiesReader.props.getBoolean(FRUIT_MACHINE, "enabled");
  }
  
  // BURIED_TREASURE
  public static boolean isBuriedTreasureEnabled() {
    return ConfigPropertiesReader.props.getBoolean(BURIED_TREASURE, "enabled");
  }
  public static int getBuriedTreasureX() {
    return ConfigPropertiesReader.props.getInt(BURIED_TREASURE, "x");
  }
  public static int getBuriedTreasureY() {
    return ConfigPropertiesReader.props.getInt(BURIED_TREASURE, "y");
  }
  
  // FISHING_VORTEX
  public static boolean isFishingVortexEnabled() {
    return ConfigPropertiesReader.props.getBoolean(FISHING_VORTEX, "enabled");
  }
  
  // COINCIDENCE
  public static boolean isCoincidenceEnabled() {
    return ConfigPropertiesReader.props.getBoolean(COINCIDENCE, "enabled");
  }
  public static int getCoincidenceMaxSpend() {
    return ConfigPropertiesReader.props.getInt(COINCIDENCE, "max_spend");
  }
  
  // LAB_RAY
  public static boolean isLabRayEnabled() {
    return ConfigPropertiesReader.props.getBoolean(LAB_RAY, "enabled");
  }
  public static String getLabRayPetName() {
    return ConfigPropertiesReader.props.getString(LAB_RAY, "pet_name");
  }
  
  // TURMACULUS
  public static boolean isTurmaculusEnabled() {
    return ConfigPropertiesReader.props.getBoolean(TURMACULUS, "enabled");
  }
  public static String getTurmaculusPetName() {
    return ConfigPropertiesReader.props.getString(TURMACULUS, "pet_name");
  }
  
  // SNOWAGER
  public static boolean isSnowagerEnabled() {
    return ConfigPropertiesReader.props.getBoolean(SNOWAGER, "enabled");
  }
  
  // EDNAS_QUEST
  public static boolean isEdnasQuestEnabled() {
    return ConfigPropertiesReader.props.getBoolean(EDNAS_QUEST, "enabled");
  }
  public static int getEdnasQuestDailyMaxSpend() {
    return ConfigPropertiesReader.props.getInt(EDNAS_QUEST, "daily_max_spend");
  }
  public static int getEdnasQuest1ItMaxCost() {
    return ConfigPropertiesReader.props.getInt(EDNAS_QUEST, "1_it_max_cost");
  }
  public static int getEdnasQuest2ItMaxCost() {
    return ConfigPropertiesReader.props.getInt(EDNAS_QUEST, "2_it_max_cost");
  }
  public static int getEdnasQuest3ItMaxCost() {
    return ConfigPropertiesReader.props.getInt(EDNAS_QUEST, "3_it_max_cost");
  }
  
  // WISE_OLD_KING
  public static boolean isWiseOldKingEnabled() {
    return ConfigPropertiesReader.props.getBoolean(WISE_OLD_KING, "enabled");
  }
  // COLTZANS_SHRINE
  public static boolean isColtzansShrineEnabled() {
    return ConfigPropertiesReader.props.getBoolean(COLTZANS_SHRINE, "enabled");
  }

  // FORGOTTEN_SHORE
  public static boolean isForgottenShoreEnabled() {
    return ConfigPropertiesReader.props.getBoolean(FORGOTTEN_SHORE, "enabled");
  }

  // STOCK_MARKET
  public static boolean isStockMarketEnabled() {
    return ConfigPropertiesReader.props.getBoolean(STOCK_MARKET, "enabled");
  }
  public static int getStockMarketMinBuy() {
    return ConfigPropertiesReader.props.getInt(STOCK_MARKET, "min_buy");
  }
  public static int getStockMarketMaxBuy() {
    return ConfigPropertiesReader.props.getInt(STOCK_MARKET, "max_buy");
  }
  public static double getStockMarketMinSell() {
    return ConfigPropertiesReader.props.getDouble(STOCK_MARKET, "min_sell");
  }
  
  // SHOP_WIZARD
  public static int getShopWizSearchTimes() {
    return ConfigPropertiesReader.props.getInt(SHOP_WIZARD, "search_times");
  }

  // MY_SHOP
  public static double getMyShopPercent() {
    return ConfigPropertiesReader.props.getDouble(MY_SHOP, "percent");
  }
  public static boolean isMyShopUseJellyneo() {
    return ConfigPropertiesReader.props.getBoolean(MY_SHOP, "use_jellyneo");
  }
  public static boolean isMyShopUsingPin() {
    return ConfigPropertiesReader.props.getBoolean(MY_SHOP, "using_pin");
  }

  // ORGANIZE_INVENTORY
  public static boolean isOrganizeInventoryEnabled() {
    return ConfigPropertiesReader.props.getBoolean(ORGANIZE_INVENTORY, "enabled");
  }
  public static int getStockMinPrice() {
    return ConfigPropertiesReader.props.getInt(ORGANIZE_INVENTORY, "min_price");
  }
  
  // FAERIE QUEST
  public static boolean isFaerieQuestEnabled() {
    return ConfigPropertiesReader.props.getBoolean(FAERIE_QUEST, "enabled");
  }
  public static int getFaerieQuestMaxCost() {
    return ConfigPropertiesReader.props.getInt(FAERIE_QUEST, "max_cost", Integer.MAX_VALUE);
  }
  public static List<String> getFaeriesToAccept() {
    return ConfigPropertiesReader.props.getList(FAERIE_QUEST, "accept", ",", null);
  }
  public static List<String> getFaeriesToReject() {
    return ConfigPropertiesReader.props.getList(FAERIE_QUEST, "reject", ",", Arrays.asList());
  }
  public static String getSideUsername() {
    return ConfigPropertiesReader.props.getString(FAERIE_QUEST, "username");
  }
  public static String getSidePassword() {
    return ConfigPropertiesReader.props.getString(FAERIE_QUEST, "password");
  }

  // LOGGING
  public static boolean isLogCaptchaImages() {
    return ConfigPropertiesReader.props.getBoolean(LOGGING, "log_captcha_images");
  }
  public static boolean isLogRequests() {
    return ConfigPropertiesReader.props.getBoolean(LOGGING, "log_requests");
  }

  // DEBUG
  public static boolean isDebugEnabled() {
    return ConfigPropertiesReader.props.getBoolean(DEBUG, "enabled");
  }
  public static double getDelay() {
    return ConfigPropertiesReader.props.getDouble(DEBUG, "delay", 0);
  }
  
  // STATUS
  public static Status getStatus() {
    return Status.get(ConfigPropertiesReader.props.getString(STATUS, "status"));
  }
  
  public static void updateStatus(Status status) {
    ConfigPropertiesReader.props.addString(STATUS, "status", status.name().toLowerCase());
  }
  
  // BOT
  public static String TOKEN = ConfigPropertiesReader.props.getString(BOT, "token");
  public static int DISCORD_MAX_LENGTH = 2000;
}
