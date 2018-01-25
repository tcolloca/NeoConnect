package com.neopetsconnect.utils;

import java.util.Arrays;
import java.util.Locale;

import com.logger.main.CategoryLogger;

public class Logger implements Categories {
	
	public static final CategoryLogger out;
	
	static { 
		Locale.setDefault(Locale.ENGLISH);
		out = new CategoryLogger(System.out).setCategoryFormat("[%s] ")
				.setDefaultCategory(MAIN);
		
		out.addCategoryChildren(DAILIES, Arrays.asList(new String[]{QUESTS,	KITCHEN_QUEST, FOOD_CLUB, 
			TDMGPOP, HEALING_SPRINGS, FISHING_VORTEX, COINCIDENCE, FRUIT_MACHINE, BURIED_TREASURE,
			TRAINING_SCHOOL, LAB_RAY, TURMACULUS, SNOWAGER,	EDNAS_QUEST, WISE_OLD_KING,	COLTZANS_SHRINE, 
			FORGOTTEN_SHORE, STOCK_MARKET, FAERIE_QUEST}));
		
		out.addFileLog(DAILIES, "dailies.log");
		out.addFileLog(SHOP_WIZARD, "dailies.log");
		out.addFileLog(EXCEPTION, "exception.log");
		out.addFileLog(RESTOCK, "restock.log");
		out.addFileLog(BUY, "buy.log");
		out.addFileLog(INVENTORY, "inventory.log");
	}
	
	private Logger() {}
}
