package com.neopetsconnect.dailies;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import com.neopetsconnect.shopwizard.ShopItem;
import com.neopetsconnect.shopwizard.ShopWizard;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class Coincidence implements ConfigProperties {

  private final static String CATEGORY = COINCIDENCE;

  private final HttpHelper helper;
  private final ShopWizard shopWizard;

  public Coincidence(HttpHelper helper) {
    this.helper = helper;
    this.shopWizard = new ShopWizard(helper);
  }

  public static void main(String[] args) {
    HttpHelper helper = Main.initHelper();
    Main.handleSession(helper);

    Coincidence daily = new Coincidence(helper);
    daily.call();
  }

  public int call() {
    Status status;
    Status prev = null;
    while (!((status = getStatus()) instanceof WaitingStatus) && !status.equals(prev)) {
      Logger.out.log(CATEGORY, status.getClass().getSimpleName());
      if (status instanceof CompletedStatus) {
        Logger.out.log(CATEGORY, "Gave items :)");
        giveItems();
      }
      if (status instanceof NoneStatus) {
        Logger.out.log(CATEGORY, "Asked new quest.");
        askQuest();
      }
      if (status instanceof PendingPayStatus) {
        pay((PendingPayStatus) status);
      }
    }
    if (status.equals(prev)) {
      throw new RuntimeException("Looping at training school");
    }
    Logger.out.logTime(CATEGORY, "Have to wait %.0f hours.",
        ((WaitingStatus) status).getWaitTime().toSecondOfDay(), TimeUnits.SECONDS, TimeUnits.HOURS);
    return ((WaitingStatus) status).getWaitTime().toSecondOfDay() + 60 * 5;
  }

  private void pay(PendingPayStatus status) {
    List<Item> items = ((PendingPayStatus) status).getItems();
    Logger.out.log(CATEGORY, "Needed: " + items);
    Map<Item, List<ShopItem>> bought =
        shopWizard.buyIfWorthIt(items, COINCIDENCE_MAX_SPEND, SHOP_WIZ_SEARCH_TIMES);
    if (bought.isEmpty()) {
      sayNo(((PendingPayStatus) status).getKey());
    }
    boolean succeeded = bought.entrySet().stream()
        .map(entry -> entry.getKey().getAmount() == entry.getValue().size())
        .reduce(true, (x, y) -> x && y);
    if (!succeeded) {
      Logger.out.log(CATEGORY, "Couldn't buy all :|");
    }
  }

  private Status getStatus() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Document doc = httpUtils.getDocument(mainRequest());
      Element questItems = doc.getElementById("questItems");
      Element handItemsForm = doc.getElementById("shipQuest");
      if (questItems != null) {
        String script = HttpUtils.getContent(doc).select(":root > script").get(2).toString();
        String _ref_ck = Utils.between(script, "_ref_ck: \"", "\"").trim();
        List<Item> items = questItems.select(":root > tbody > tr > td").stream().map(elem -> {
          String text = elem.text();
          int xPos = text.lastIndexOf("x");
          String name = text.substring(0, xPos);
          int amount = Integer.parseInt(text.substring(xPos + 1));
          return new Item(name).setAmount(amount);
        }).collect(Collectors.toList());
        return new PendingPayStatus(items, _ref_ck);
      } else if (handItemsForm != null) {
        return new CompletedStatus();
      } else {
        String text = HttpUtils.getContent(doc).text();
        if (text.contains("RESEARCH COMPLETE IN")) {
          Element clock = HttpUtils.getContent(doc).select(":root > div > script").get(0);
          String timeStr =
              Utils.between(clock.toString(), "\\{", "tick").replaceAll("(\r|\n|\t| )", "");
          DateTimeFormatter formatter =
              DateTimeFormatter.ofPattern("'hours':H,'minutes':m,'seconds':s,");
          TemporalAccessor time = formatter.parse(timeStr);
          return new WaitingStatus(Utils.toLocalTime(time));
        }
        return new NoneStatus();
      }
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
    // TODO : Wait
  }

  private boolean askQuest() {
    return loadAskQuest().getStatusCode() == 200;
  }

  private boolean sayNo(String key) {
    return loadSayNo(key).getStatusCode() == 200;
  }

  private boolean giveItems() {
    return postItems().getStatusCode() == 200;
  }

  private HttpResponse loadAskQuest() {
    return helper.get("/magma/portal/ship.phtml")
        .addHeader(Headers.referer("http://www.neopets.com/space/coincidence.phtml")).send();
  }

  private HttpResponse loadSayNo(String _ref_ck) {
    return helper.post("/magma/portal/ajax/cancelQuest.php")
        .addHeader(Headers.referer("http://www.neopets.com/space/coincidence.phtml"))
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addFormParameter("_ref_ck", _ref_ck).send();
  }

  private HttpResponse postItems() {
    return helper.post("/space/coincidence.phtml")
        .addHeader(Headers.referer("http://www.neopets.com/space/coincidence.phtml"))
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addFormParameter("showRe", 1).send();
  }

  private HttpRequest mainRequest() {
    return helper.get("/space/coincidence.phtml");
  }
}
