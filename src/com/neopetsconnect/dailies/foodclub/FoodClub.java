package com.neopetsconnect.dailies.foodclub;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.Parameter;
import com.neopetsconnect.main.Main;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class FoodClub implements ConfigProperties {

  private static final String CATEGORY = FOOD_CLUB;
  private final HttpHelper helper;

  public FoodClub(HttpHelper helper) {
    this.helper = helper;
  }

  public static void main(String[] args) {
    HttpHelper helper = Main.initHelper();
    Main.handleSession(helper);

    FoodClub daily = new FoodClub(helper);
    FoodClubBets bets = daily.getLeftysBetInfo();
    daily.bet(bets);
  }

  public int call() {
    FoodClubBets bets = getLeftysBetInfo();
    if (!bet(bets)) {
      return DAILIES_REFRESH_FREQ / 2; // TODO : Configure
    }
    Logger.out.log(CATEGORY, "Done.");
    return DAILIES_REFRESH_FREQ;
  }

  private boolean bet(FoodClubBets bets) {
    if (DailyLog.props.getBoolean(CATEGORY, "done", false)) {
      return true;
    }
    LocalDate foodClubDate = Utils.neopetsNow().toLocalDate();
    if (Utils.neopetsNow().toLocalTime().isAfter(LocalTime.of(13, 40))) {
      foodClubDate = foodClubDate.plusDays(1);
    }
    Logger.out.log(CATEGORY, "Today: " + foodClubDate + " Lefty's: " + bets.getDate());
    if (bets.getDate().equals(foodClubDate)) {
      FoodClubOdds foodClubOdds = parseCurrentOdds();
      for (int i = 0; i < bets.getBetInfos().size(); i++) {
        FoodClubPostForm betForm = new FoodClubPostForm(foodClubOdds, bets.getBetInfos().get(i));
        Logger.out.log(CATEGORY, betForm.toFormParameters());
        placeBet(betForm);
      }
      Logger.out.log(CATEGORY, "Done :D");
      DailyLog.props.addBoolean(CATEGORY, "done", true);
      return true;
    }
    return false;
  }

  private FoodClubOdds parseCurrentOdds() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(foodClubPageRequest());
      String amountStr = content.select(":root > p").get(3).text();
      int maxBet = Integer.parseInt(amountStr.split("up to ")[1].split(" NeoPoints")[0]);
      Element formTable = content.select(":root > center > form > table").get(0);
      List<Element> rows = formTable.select(":root > tbody > tr");
      rows.remove(0);
      rows.remove(0);
      List<AreaOdds> areaOdds = new ArrayList<>();
      for (int i = 0; i < rows.size(); i++) {
        List<Element> cols = rows.get(i).select(":root > td");
        String area = cols.get(0).text();
        List<Element> options = cols.get(1).select(":root > select > option");
        options.remove(0);
        List<PlayerOdds> playerOdds = options.stream().map(option -> {
          int id = Integer.parseInt(option.attr("value"));
          String[] aux = option.text().split("\\(");
          String player = aux[0].replaceAll("  ", "").trim();
          int odds = Integer.parseInt(aux[1].split(":")[0]);
          return new PlayerOdds(id, player, odds);
        }).collect(Collectors.toList());
        areaOdds.add(new AreaOdds(i + 1, area, playerOdds));
      }
      return new FoodClubOdds(maxBet, areaOdds);
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private void placeBet(FoodClubPostForm form) {
    HttpRequest req = helper.post("/pirates/process_foodclub.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/pirates/foodclub.phtml?type=bet"));
    for (Parameter param : form.toFormParameters()) {
      req.addFormParameter(param);
    }
    req.send();
  }

  private HttpRequest foodClubPageRequest() {
    return helper.get("/pirates/foodclub.phtml").addQueryParameter("type", "bet");
  }

  private FoodClubBets getLeftysBetInfo() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    Document doc = httpUtils.getDocument(leftysPageRequest());
    try {
      Element content = doc.getElementById("content");
      String dateStr = content.select("h1").get(1).text().split("-")[0].trim();
      DateTimeFormatter formatter =
          DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
      LocalDate date = LocalDate.parse(dateStr, formatter);

      List<Element> rows = content.select(":root > center > center > table > tbody > tr");

      rows.remove(0);
      rows.remove(0);
      rows.remove(rows.size() - 1);

      List<BetInfo> betInfo = rows.stream().map(row -> {
        String betInfoStr = row.select(":root > td").get(1).toString();
        betInfoStr = betInfoStr.replaceAll("(<td>|</td>|<b>|</b>)", "");
        List<AreaInfo> areaInfos = Arrays.stream(betInfoStr.split("<br>")).map(areaInfo -> {
          String[] arr = areaInfo.split(": ");
          String area = arr[0];
          String player = arr[1];
          return new AreaInfo(area, player);
        }).collect(Collectors.toList());
        return new BetInfo(areaInfos);
      }).collect(Collectors.toList());
      return new FoodClubBets(date, betInfo);
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpRequest leftysPageRequest() {
    return helper.get("/~Innocent");
  }
}
