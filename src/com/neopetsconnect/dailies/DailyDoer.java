package com.neopetsconnect.dailies;

import com.httphelper.main.HttpHelper;
import com.neopetsconnect.dailies.foodclub.FoodClub;
import com.neopetsconnect.dailies.trainingschool.TrainingSchool;
import com.neopetsconnect.inventory.Inventory;
import com.neopetsconnect.myshop.MyShop;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.TimedJob;
import com.neopetsconnect.utils.TimedJobsRunner;

public class DailyDoer implements ConfigProperties {

	private final TimedJobsRunner<Integer> runner;
	
	public DailyDoer(HttpHelper helper) {
		this.runner = new TimedJobsRunner<Integer>((callable, remaining) -> new TimedJob<Integer>(callable, remaining));
		
		runner.addAlwaysJob(() -> { DailyLog.createIfNecessary(); return 0; });
		if (ORGANIZE_INVENTORY_ENABLED) {
			runner.addAlwaysJob(() -> { new Inventory(helper).organize(STOCK_MIN_PRICE); return 0; });
		}
		runner.addJob(() -> {
			new MyShop(helper).updatePrices(SHOP_WIZ_SEARCH_TIMES, 
					true, MY_SHOP_USE_JELLYNEO, 2, 
					MY_SHOP_PERCENT);
			return 60 * 60 * 4;
		}, 0);
		if (STOCK_MARKET_ENABLED) {
			runner.addJob(() -> new StockMarket(helper).call(), 0);
		}
		if (FORGOTTEN_SHORE_ENABLED) {
			runner.addJob(() -> new ForgottenShore(helper).call(), 0);
		}
		if (COLTZANS_SHRINE_ENABLED) {
			runner.addJob(() -> new ColtzanShrine(helper).call(), 0);
		}
		if (WISE_OLD_KING_ENABLED) {
			runner.addJob(() -> new WiseOldKing(helper).call(), 0);
		}
		if (TURMACULUS_ENABLED) {
			runner.addJob(() -> new Turmaculus(helper).call(), 0);
		}
		if (LAB_RAY_ENABLED) {
			runner.addJob(() -> new LabRay(helper).call(), 0);
		}
		if (EDNAS_QUEST_ENABLED) {
			runner.addJob(() -> new EdnasQuest(helper).call(), 0);
		}
		if (KITCHEN_QUEST_ENABLED) {
			runner.addJob(() -> new KitchenQuest(helper).call(), 0);
		}
		if (FOOD_CLUB_ENABLED) {
			runner.addJob(() -> new FoodClub(helper).call(), 0);
		}
		if (TRAINING_SCHOOL_ENABLED) {
			runner.addJob(() -> new TrainingSchool(helper).call(), 0);
		}
		if (TDMGPOP_ENABLED) {
			runner.addJob(() -> new TDMGPoP(helper).call(), 0);
		}
		if (HEALING_SPRINGS_ENABLED) {
			runner.addJob(() -> new HealingSprings(helper).call(), 0);
		}
		if (FISHING_VORTEX_ENABLED) {
			runner.addJob(() -> new YeOldeFishingVortex(helper).call(), 0);
		}
		if (FRUIT_MACHINE_ENABLED) {
			runner.addJob(() -> new FruitMachine(helper).call(), 0);
		}
		if (COINCIDENCE_ENABLED) {
			runner.addJob(() -> new Coincidence(helper).call(), 0);
		}
		if (BURIED_TREASURE_ENABLED) {
			runner.addJob(() -> new BuriedTreasure(helper).call(), 0);
		}
		if (SNOWAGER_ENABLED) {
			runner.addJob(() -> new Snowager(helper).call(), 0);
		}
	}
	
	/**
	 * Blocking Function.
	 * @throws Exception 
	 */
	public void run() throws Exception {
		runner.run();
	}
}
