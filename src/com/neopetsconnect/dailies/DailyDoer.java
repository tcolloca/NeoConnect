package com.neopetsconnect.dailies;

import com.httphelper.main.HttpHelper;
import com.neopetsconnect.dailies.foodclub.FoodClub;
import com.neopetsconnect.dailies.trainingschool.TrainingSchool;
import com.neopetsconnect.inventory.Inventory;
import com.neopetsconnect.myshop.MyShop;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.TimedJob;
import com.neopetsconnect.utils.TimedJobsRunner;

public class DailyDoer implements Categories {

  private final TimedJobsRunner<Integer> runner;

  public DailyDoer(HttpHelper helper) {
    this.runner = new TimedJobsRunner<Integer>(
        (callable, remaining) -> new TimedJob<Integer>(callable, remaining));

    runner.addAlwaysJob(() -> {
      DailyLog.createIfNecessary();
      return 0;
    });

    runner.addJob(() -> {
      new Inventory(helper).organize(ConfigProperties.getInventoryMinPrice());
      return 60 * 60 * 4;
    }, 0);

    runner.addJob(() -> {
      new MyShop(helper).updatePrices(ConfigProperties.getShopWizSearchTimes(), true,
          ConfigProperties.isMyShopUseJellyneo(), 2, ConfigProperties.getMyShopPercent());
      return 60 * 60 * 4;
    }, 0);

    runner.addJob(() -> new DeadlyDice(helper).call(), 0);
    runner.addJob(() -> new Bank(helper).call(), 0);
    runner.addJob(() -> new StockMarket(helper).call(), 0);
    runner.addJob(() -> new ForgottenShore(helper).call(), 0);
    runner.addJob(() -> new ColtzanShrine(helper).call(), 0);
    runner.addJob(() -> new WiseOldKing(helper).call(), 0);
    runner.addJob(() -> new Turmaculus(helper).call(), 0);
    runner.addJob(() -> new LabRay(helper).call(), 0);
    runner.addJob(() -> new EdnasQuest(helper).call(), 0);
    runner.addJob(() -> new KitchenQuest(helper).call(), 0);
    runner.addJob(() -> new FoodClub(helper).call(), 0);
    runner.addJob(() -> new TrainingSchool(helper).call(), 0);
    runner.addJob(() -> new TDMGPoP(helper).call(), 0);
    runner.addJob(() -> new HealingSprings(helper).call(), 0);
    runner.addJob(() -> new YeOldeFishingVortex(helper).call(), 0);
    runner.addJob(() -> new FruitMachine(helper).call(), 0);
    runner.addJob(() -> new Coincidence(helper).call(), 0);
    runner.addJob(() -> new BuriedTreasure(helper).call(), 0);
    runner.addJob(() -> new Snowager(helper).call(), 0);
    runner.addJob(() -> new DeadlyDice(helper).call(), 0);
  }

  /**
   * Blocking Function.
   * 
   * @throws Exception
   */
  public void run() throws Exception {
    runner.run();
  }
}
