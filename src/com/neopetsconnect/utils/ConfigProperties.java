package com.neopetsconnect.utils;

import com.neopetsconnect.main.Status;
import com.neopetsconnect.stores.NeopetsStoreId;

public interface ConfigProperties extends Categories {

	// MAIN
	int DAILIES_WAIT_SLEEP = ConfigPropertiesReader.props.getInt(DAILIES, "dailies_wait_sleep");
	
	// SESSION
	String USERNAME = ConfigPropertiesReader.props.getString(SESSION, "username");
	String PASSWORD = ConfigPropertiesReader.props.getString(SESSION, "password");
	String PIN = ConfigPropertiesReader.props.getString(SESSION, "pin");

	
	// RESTOCK
	boolean RESTOCK_ENABLED = ConfigPropertiesReader.props.getBoolean(RESTOCK, "enabled");

	String STORE_NAME = ConfigPropertiesReader.props.getString(RESTOCK, "store");
	int STORE_ID = NeopetsStoreId.getByName(STORE_NAME).id;
	
	int MIN_PROFIT = ConfigPropertiesReader.props.getInt(RESTOCK, "min_profit");
	int MIN_STOP_PROFIT = ConfigPropertiesReader.props.getInt(RESTOCK, "min_stop_profit");
			
	int MAX_RESTOCK_TIME = ConfigPropertiesReader.props.getInt(RESTOCK, "max_restock_time");
	int MAX_TIME_SLEEP = ConfigPropertiesReader.props.getInt(RESTOCK, "max_time_sleep");
	int CHECK_BAN_TIME = ConfigPropertiesReader.props.getInt(RESTOCK, "check_ban_time");
	int AFTER_BOUGHT_SLEEP_TIME = ConfigPropertiesReader.props.getInt(RESTOCK, "after_bought_sleep_time");
	double MIN_REFRESH_TIME = ConfigPropertiesReader.props.getDouble(RESTOCK, "min_refresh_time");
	double MAX_REFRESH_TIME = ConfigPropertiesReader.props.getDouble(RESTOCK, "max_refresh_time");
	
	int BAN_SLEEP_TIME = ConfigPropertiesReader.props.getInt(RESTOCK, "ban_sleep_time");
	int BAN_SLEEP_FACTOR = ConfigPropertiesReader.props.getInt(RESTOCK, "ban_sleep_factor");
	int MIN_SOLD_OUT_WAIT_TIME = ConfigPropertiesReader.props.getInt(RESTOCK, "min_sold_out_wait_time");
	int MAX_SOLD_OUT_WAIT_TIME = ConfigPropertiesReader.props.getInt(RESTOCK, "max_sold_out_wait_time");
	
	// DAILIES
	
	String DAILIES_LOG_PATH = ConfigPropertiesReader.props.getString(DAILIES, "dailies_log_path");
	String DAILIES_LOG_FILE_FORMAT = "dd-MM-yyyy"; 
	int DAILIES_REFRESH_FREQ = ConfigPropertiesReader.props.getInt(DAILIES, "refresh_freq");

	
	// KITCHEN QUEST
	boolean KITCHEN_QUEST_ENABLED = ConfigPropertiesReader.props.getBoolean(KITCHEN_QUEST, "enabled");
	int KITCHEN_QUEST_DAILY_MAX_SPEND = ConfigPropertiesReader.props.getInt(KITCHEN_QUEST, "daily_max_spend");
	int KITCHEN_QUEST_MAX_COST = ConfigPropertiesReader.props.getInt(KITCHEN_QUEST, "max_cost");
	
	// FOOD_CLUB
	boolean FOOD_CLUB_ENABLED = ConfigPropertiesReader.props.getBoolean(FOOD_CLUB, "enabled");
	
	// TRAINING_SCHOOL
	boolean TRAINING_SCHOOL_ENABLED = ConfigPropertiesReader.props.getBoolean(TRAINING_SCHOOL, "enabled");
	String STAT_TYPE = ConfigPropertiesReader.props.getString(TRAINING_SCHOOL, "stat_type");
	
	// TDMGPOP
	boolean TDMGPOP_ENABLED = ConfigPropertiesReader.props.getBoolean(TDMGPOP, "enabled");
	
	// HEALING_SPRINGS
	boolean HEALING_SPRINGS_ENABLED = ConfigPropertiesReader.props.getBoolean(HEALING_SPRINGS, "enabled");
	int HEALING_SPRINGS_FREQ = ConfigPropertiesReader.props.getInt(HEALING_SPRINGS, "frequency");
	
	// FRUIT_MACHINE
	boolean FRUIT_MACHINE_ENABLED = ConfigPropertiesReader.props.getBoolean(FRUIT_MACHINE, "enabled");
	
	// BURIED_TREASURE
	boolean BURIED_TREASURE_ENABLED = ConfigPropertiesReader.props.getBoolean(BURIED_TREASURE, "enabled");
	int BURIED_TREASURE_X = ConfigPropertiesReader.props.getInt(BURIED_TREASURE, "x");
	int BURIED_TREASURE_Y = ConfigPropertiesReader.props.getInt(BURIED_TREASURE, "y");
	
	// FISHING_VORTEX
	boolean FISHING_VORTEX_ENABLED = ConfigPropertiesReader.props.getBoolean(FISHING_VORTEX, "enabled");
	
	// COINCIDENCE
	boolean COINCIDENCE_ENABLED = ConfigPropertiesReader.props.getBoolean(FISHING_VORTEX, "enabled");
	int COINCIDENCE_MAX_SPEND = ConfigPropertiesReader.props.getInt(COINCIDENCE, "max_spend");
	
	// LAB_RAY
	boolean LAB_RAY_ENABLED = ConfigPropertiesReader.props.getBoolean(LAB_RAY, "enabled");
	String LAB_RAY_PET_NAME = ConfigPropertiesReader.props.getString(LAB_RAY, "pet_name");
	
	// TURMACULUS
	boolean TURMACULUS_ENABLED = ConfigPropertiesReader.props.getBoolean(TURMACULUS, "enabled");
	String TURMACULUS_PET_NAME = ConfigPropertiesReader.props.getString(TURMACULUS, "pet_name");
	
	// SNOWAGER
	boolean SNOWAGER_ENABLED = ConfigPropertiesReader.props.getBoolean(SNOWAGER, "enabled");
	
	// EDNAS_QUEST
	boolean EDNAS_QUEST_ENABLED = ConfigPropertiesReader.props.getBoolean(EDNAS_QUEST, "enabled");
	int EDNAS_QUEST_DAILY_MAX_SPEND = ConfigPropertiesReader.props.getInt(EDNAS_QUEST, "daily_max_spend");
	int EDNAS_QUEST_1_IT_MAX_COST = ConfigPropertiesReader.props.getInt(EDNAS_QUEST, "1_it_max_cost");
	int EDNAS_QUEST_2_IT_MAX_COST = ConfigPropertiesReader.props.getInt(EDNAS_QUEST, "2_it_max_cost");
	int EDNAS_QUEST_3_IT_MAX_COST = ConfigPropertiesReader.props.getInt(EDNAS_QUEST, "3_it_max_cost");
	
	// WISE_OLD_KING
	boolean WISE_OLD_KING_ENABLED = ConfigPropertiesReader.props.getBoolean(WISE_OLD_KING, "enabled");
	
	// COLTZANS_SHRINE
	boolean COLTZANS_SHRINE_ENABLED = ConfigPropertiesReader.props.getBoolean(COLTZANS_SHRINE, "enabled");
	
	// FORGOTTEN_SHORE
	boolean FORGOTTEN_SHORE_ENABLED = ConfigPropertiesReader.props.getBoolean(FORGOTTEN_SHORE, "enabled");
	
	// STOCK_MARKET
	boolean STOCK_MARKET_ENABLED = ConfigPropertiesReader.props.getBoolean(STOCK_MARKET, "enabled");
	int STOCK_MARKET_MIN_BUY = ConfigPropertiesReader.props.getInt(STOCK_MARKET, "min_buy");
	int STOCK_MARKET_MAX_BUY = ConfigPropertiesReader.props.getInt(STOCK_MARKET, "max_buy");
	double STOCK_MARKET_MIN_SELL = ConfigPropertiesReader.props.getDouble(STOCK_MARKET, "min_sell");
	
	// SHOP_WIZARD
	int SHOP_WIZ_SEARCH_TIMES = ConfigPropertiesReader.props.getInt(SHOP_WIZARD, "search_times");
	
	// MY_SHOP
	double MY_SHOP_PERCENT = ConfigPropertiesReader.props.getDouble(MY_SHOP, "percent");
	boolean MY_SHOP_USE_JELLYNEO = ConfigPropertiesReader.props.getBoolean(MY_SHOP, "percent");
	boolean MY_SHOP_USING_PIN = ConfigPropertiesReader.props.getBoolean(MY_SHOP, "using_pin");
	
	// ORGANIZE_INVENTORY
	boolean ORGANIZE_INVENTORY_ENABLED = ConfigPropertiesReader.props.getBoolean(ORGANIZE_INVENTORY, "enabled");
	int STOCK_MIN_PRICE = ConfigPropertiesReader.props.getInt(ORGANIZE_INVENTORY, "min_price");
	
	// LOGGING
	boolean LOG_CAPTCHA_IMAGES = ConfigPropertiesReader.props.getBoolean(LOGGING, "log_captcha_images");
	boolean LOG_REQUESTS = ConfigPropertiesReader.props.getBoolean(LOGGING, "log_requests");
	
	// BOT
	String TOKEN = ConfigPropertiesReader.props.getString(BOT, "token");
	int DISCORD_MAX_LENGTH = 2000;
	
	// STATUS
	static Status getStatus() {
		return Status.get(ConfigPropertiesReader.props.getString(STATUS, "status"));
	}
	
	static void updateStatus(Status status) {
		ConfigPropertiesReader.props.addString(STATUS, "status", status.name().toLowerCase());
	}
}
