package com.neopetsconnect.restock;

import java.util.List;
import java.util.stream.Collectors;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.logger.main.TimeUnits;
import com.neopetsconnect.exceptions.RestockBannedException;
import com.neopetsconnect.inventory.Inventory;
import com.neopetsconnect.main.Status;
import com.neopetsconnect.shopwizard.ShopItem;
import com.neopetsconnect.stores.NeopetsStore;
import com.neopetsconnect.stores.NeopetsStoreId;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class Restocker implements Categories {

  private final HttpHelper helper;
  private NeopetsStore store;

  public Restocker(HttpHelper helper) {
    this.helper = helper;
    this.store = new NeopetsStore(helper, ConfigProperties.getStoreId());
  }

  public void restockLoop() {
    while (!ConfigProperties.getStatus().isStoppedOrOff()) {
      if (ConfigProperties.getStatus().equals(Status.PAUSED)) {
        Utils.sleep(0.1);
        continue;
      }
      try {
          restock();
      } catch (RestockBannedException e) {
        int i = 0;
        do {
          Logger.out.log(RESTOCK, "Banned!");
          Utils.sleepAndLog(RESTOCK, ConfigProperties.getBanSleepTime() * 
              (Math.pow(ConfigProperties.getBanSleepFactor(), i++)),
              TimeUnits.MINUTES);
        } while (checkBan());
      }
    }
  }

  /**
   * Blocking function.
   */
  private void restock() {
    while (!ConfigProperties.getStatus().isStoppedOrOff()) {
      if (!ConfigProperties.isRestockEnabled()) {
        if (ConfigProperties.getStatus().isStoppedOrOff()) {
          break;
        }
        Utils.sleep(0.1);
        continue;
      }
      try {
        Logger.out.logTimeStart("", TimeUnits.SECONDS);
        List<ShopItem> list = reload();
        Logger.out.logTimeEnd("", RESTOCK, "Reload store items time: %.3f");
        if (list == null) {
          continue;
        }
        buy(list);
      } catch (RestockBannedException e) {
        throw (RestockBannedException) e;
      } catch (HttpHelperException e) {
        Logger.out.log(MAIN, "Connection failed: " + e.getMessage());
      }
    }
  }

  private List<ShopItem> reload() {
    System.out.println("reload");
    long time = 0, startTime = System.currentTimeMillis();
    long rsBanStartTime = System.currentTimeMillis();

    boolean soldOutWaiting = false;

    while (time < ConfigProperties.getMaxRestockTime() * 1000
        && !ConfigProperties.getStatus().isStoppedOrOff()) {
      if (ConfigProperties.getStatus().equals(Status.PAUSED)) {
        Utils.sleep(0.01);
        continue;
      }
      Logger.out.logTimeStart("", TimeUnits.SECONDS);
      List<ShopItem> items = store.getItems();
      double loadTime = Logger.out.logTimeEnd("", RESTOCK, "Get items list: %.3f");
      if (items.isEmpty()) {
        if (!soldOutWaiting) {
          soldOutWaiting = true;
          Logger.out.log(RESTOCK, "Sold out!");
          Utils.sleepAndLog(RESTOCK, Utils.random(ConfigProperties.getMinSoldOutWaitTime(), 
              ConfigProperties.getMaxSoldOutWaitTime()));
        }
      } else {
        soldOutWaiting = false;
        List<ShopItem> worthyItems = store.getWorthyItems(items, ConfigProperties.getMinProfit());
        if (!worthyItems.isEmpty()) {
          return worthyItems;
        }
      }
      if (loadTime < ConfigProperties.getMaxRefreshTime()) {        
        Utils.sleepAndLog(RESTOCK, Utils.random(ConfigProperties.getMinRefreshTime(), 
            ConfigProperties.getMaxRefreshTime()));
      }

      time = System.currentTimeMillis() - startTime;
      if (System.currentTimeMillis() - rsBanStartTime > ConfigProperties.getCheckBanTime() * 1000) {
        rsBanStartTime = System.currentTimeMillis();
        if (checkBan()) {
          throw new RestockBannedException();
        }
      }
    }
    Logger.out.log(RESTOCK, "Max time reached.");
    Utils.sleepAndLog(RESTOCK, ConfigProperties.getMaxTimeSleep(), TimeUnits.MINUTES);
    return null;
  }

  private void buy(List<ShopItem> list) {
    long start = System.currentTimeMillis();
    list.stream().findFirst().ifPresent(item -> {
      boolean wasBought;
      Logger.out.log(RESTOCK, item);
      try {
        wasBought = store.buyItem(item);
        long end = System.currentTimeMillis();
        logBought(list, item, wasBought, (end - start) / (double) (1000));
        Logger.out.log(RESTOCK, wasBought ? "bought item" : "failed :(");
        if (wasBought) {
          Inventory inventory = new Inventory(helper);
          inventory.moveToShop(item);
          if (item.getPrice().map(price -> price > ConfigProperties.getMinStopProfit())
              .orElse(true)) {
            Utils.sleepAndLog(RESTOCK, ConfigProperties.getAfterBoughtSleepTime(),
                TimeUnits.MINUTES);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  private boolean checkBan() {
    NeopetsStore tikiTack = new NeopetsStore(helper, NeopetsStoreId.TIKI_TACK.id);
    return tikiTack.getItems().isEmpty() && store.getItems().isEmpty();
  }

  private void logBought(List<ShopItem> items, ShopItem item, boolean wasBought, double time) {
    items.add(0, item);
    Logger.out.log(BUY, "Taken time: " + time);
    Logger.out.log(BUY, "Result of buying " + item.getName() + ": " + wasBought);
    Logger.out.log(BUY, "List of items: "
        + String.join(", ", items.stream().map(it -> it.getName()).collect(Collectors.toList())));
    Logger.out.log(BUY, "---------------------");
  }
}
