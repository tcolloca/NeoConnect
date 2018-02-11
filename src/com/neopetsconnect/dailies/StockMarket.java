package com.neopetsconnect.dailies;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.dailies.stocks.Stock;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.HttpUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class StockMarket implements ConfigProperties {

  private final static String CATEGORY = STOCK_MARKET;
  private final HttpHelper helper;

  public StockMarket(HttpHelper helper) {
    this.helper = helper;
  }

  public int call() {
    if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
      // Optional<Stock> bestStock = parseList();
      // if (bestStock.isPresent()) {
      // buy(bestStock.get(), 1000);
      // Logger.out.log(CATEGORY, "Bought 100 of " + bestStock.get().getTickerName()
      // + " at " + bestStock.get().getCurr());
      // DailyLog.props.addBoolean(CATEGORY, "done", true);
      // }
    }
    Logger.out.log(CATEGORY, "Buy done.");
    List<Stock> sold = sell(STOCK_MARKET_MIN_SELL);
    if (!sold.isEmpty()) {
      sold.forEach(stock -> Logger.out.log(CATEGORY,
          "Sold " + stock.getShares() + " at " + stock.getChange()));
    }
    return DAILIES_REFRESH_FREQ;
  }

  private List<Stock> sell(double minChange) {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Document doc = httpUtils.getDocument(portfolioRequest());
      Element form = doc.getElementById("postForm");
      String _ref_ck = form.select(":root > input[name=_ref_ck]").attr("value");
      List<Stock> stocks = soldeable(doc, minChange);
      loadSell(_ref_ck, stocks);
      return stocks;
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private List<Stock> soldeable(Document doc, double minChange) {
    Element form = doc.getElementById("postForm");
    Elements tickerRows =
        form.select(":root > table > tbody > tr[style=display:none;] > td > table > tbody");
    return Utils.merge(tickerRows.stream().map(tickerElem -> {
      Elements shareRows = tickerElem.select(":root > tr");
      shareRows.remove(0);
      return IntStream.range(0, shareRows.size()).mapToObj(i -> {
        Element shareElem = shareRows.get(i);
        Elements cols = shareElem.select(":root > td");
        int shares = Integer.parseInt(cols.get(0).text().replaceAll(",", ""));
        String changeStr = cols.get(i == 0 ? 5 : 4).text().trim().replaceAll("\\%", "");
        double change = Double.parseDouble(changeStr);
        String formName = cols.get(i == 0 ? 6 : 5).select(":root > input").attr("name");
        return new Stock(shares, change, formName);
      }).collect(Collectors.toList());
    }).collect(Collectors.toList())).stream().filter(stock -> stock.getChange() >= minChange)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
  private void buy(Stock stock, int amount) {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      loadProfile(stock.getId());
      Document doc = httpUtils.getDocument(tickerRequest(stock.getId(), stock.getTickerName()));
      String _ref_ck = getRefCk(doc);
      loadBuy(_ref_ck, stock.getTickerName(), amount);
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private String getRefCk(Document doc) {
    return doc.select("input[name=_ref_ck]").attr("value");
  }

  @SuppressWarnings("unused")
  private Optional<Stock> parseList() {
    HttpUtils httpUtils = HttpUtils.newInstance();
    try {
      Element content = httpUtils.getContent(listRequest());
      Elements rows = content.select(":root > div > table > tbody > tr");
      int minBuy = STOCK_MARKET_MIN_BUY;
      int maxBuy = STOCK_MARKET_MAX_BUY;
      rows.remove(0);
      return rows.stream().map(elem -> {
        Elements cols = elem.select(":root > td");
        int id = Integer.parseInt(cols.get(1).select(":root > a").attr("href").split("id=")[1]);
        String name = cols.get(1).text();
        int curr = Integer.parseInt(cols.get(5).text());
        return new Stock(id, name, curr);
      }).filter(stock -> stock.getCurr() >= minBuy && stock.getCurr() <= maxBuy)
          .sorted(Comparator.comparing(Stock::getCurr)).findFirst();
    } catch (Throwable th) {
      httpUtils.logRequestResponse();
      throw th;
    }
  }

  private HttpResponse loadBuy(String _ref_ck, String ticker, int amount) {
    return helper.post("/process_stockmarket.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(
            Headers.referer("http://www.neopets.com/stockmarket.phtml?type=buy&ticker=" + ticker))
        .addFormParameter("_ref_ck", _ref_ck).addFormParameter("type", "buy")
        .addFormParameter("ticker_symbol", ticker).addFormParameter("amount_shares", amount).send();
  }

  private HttpResponse loadSell(String _ref_ck, List<Stock> stocks) {
    HttpRequest req = helper.post("/process_stockmarket.phtml")
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
        .addHeader(Headers.referer("http://www.neopets.com/stockmarket.phtml?type=portfolio"))
        .addFormParameter("type", "sell").addFormParameter("_ref_ck", _ref_ck);
    for (Stock stock : stocks) {
      req.addFormParameter(stock.getFormName(), stock.getShares());
    }
    return req.send();
  }

  private HttpRequest tickerRequest(int id, String ticker) {
    return helper.get("/stockmarket.phtml?type=buy&ticker=" + ticker).addHeader(
        Headers.referer("http://www.neopets.com/stockmarket.phtml?type=profile&company_id=" + id));
  }

  private HttpResponse loadProfile(int id) {
    return helper.get("/stockmarket.phtml?type=profile&company_id=" + id)
        .addHeader(Headers.referer("http://www.neopets.com/stockmarket.phtml?type=list&full=true"))
        .send();
  }

  private HttpRequest portfolioRequest() {
    return helper.get("/stockmarket.phtml?type=portfolio");
  }

  private HttpRequest listRequest() {
    return helper.get("/stockmarket.phtml?type=list&full=true");
  }
}
