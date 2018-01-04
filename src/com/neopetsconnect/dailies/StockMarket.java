package com.neopetsconnect.dailies;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;
import com.neopetsconnect.dailies.stocks.Stock;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DailyLog;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

public class StockMarket implements ConfigProperties {

	private final static String CATEGORY = STOCK_MARKET;
	private final HttpHelper helper;
	
	public StockMarket(HttpHelper helper) {
		this.helper = helper;
	}
	
	public int call() {
		try {
			if (!DailyLog.props.getBoolean(CATEGORY, "done", false)) {
				Optional<Stock> bestStock = parseList();
				if (bestStock.isPresent()) {
					buy(bestStock.get(), 1000);
					Logger.out.log(CATEGORY, "Bought 100 of " + bestStock.get().getTickerName() 
							+ " at " + bestStock.get().getCurr());
					DailyLog.props.addBoolean(CATEGORY, "done", true);
				}
			}
			Logger.out.log(CATEGORY, "Buy done.");
			List<Stock> sold = sell(STOCK_MARKET_MIN_SELL);
			if (!sold.isEmpty()) {
				sold.forEach(stock -> Logger.out.log(CATEGORY, "Sold " + stock.getShares() + " at " + stock.getChange()));
			}
		} catch (HttpHelperException e) {
			throw new RuntimeException("Something when wrong.", e);
		}
		return DAILIES_REFRESH_FREQ;
	}
	
	private List<Stock> sell(double minChange) throws HttpHelperException {
		String content = loadPortfolio().getContent().get();
		Document doc = Jsoup.parse(content);
		Element form = doc.getElementById("postForm");
		String _ref_ck = form.select(":root > input[name=_ref_ck]").attr("value");
		List<Stock> stocks = soldeable(doc, minChange);
		loadSell(_ref_ck, stocks);
		return stocks;
	}
	
	private List<Stock> soldeable(Document doc, double minChange)  {
		Element form = doc.getElementById("postForm");
		Elements tickerRows = form.select(":root > table > tbody > tr[style=display:none;] > td > table > tbody");
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
				}).collect(Collectors.toList())).stream()
				.filter(stock -> stock.getChange() >= minChange)
				.collect(Collectors.toList());
	}
	
	private void buy(Stock stock, int amount) throws HttpHelperException {
		loadProfile(stock.getId());
		String _ref_ck = getRefCk(loadTicker(stock.getId(), stock.getTickerName()));
		loadBuy(_ref_ck, stock.getTickerName(), amount);
	}

	private String getRefCk(HttpResponse resp) {
		String content = resp.getContent().get();
		Document doc = Jsoup.parse(content);
		return doc.select("input[name=_ref_ck]").attr("value");
	}
	
	private Optional<Stock> parseList() throws HttpHelperException {
		String content = loadList().getContent().get();
		Document doc = Jsoup.parse(content);
		Elements rows = DomUtils.getContent(doc).select(":root > div > table > tbody > tr");
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
			.sorted(Comparator.comparing(Stock::getCurr))
			.findFirst();
	}
	
	private HttpResponse loadBuy(String _ref_ck, String ticker, int amount) throws HttpHelperException {
		return helper.post("/process_stockmarket.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/stockmarket.phtml?type=buy&ticker=" + ticker))
				.addFormParameter("_ref_ck", _ref_ck)
				.addFormParameter("type", "buy")
				.addFormParameter("ticker_symbol", ticker)
				.addFormParameter("amount_shares", amount)
				.send();
	}
	
	private HttpResponse loadSell(String _ref_ck, List<Stock> stocks) throws HttpHelperException {
		HttpRequest req = helper.post("/process_stockmarket.phtml")
				.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
				.addHeader(Headers.referer("http://www.neopets.com/stockmarket.phtml?type=portfolio"))
				.addFormParameter("type", "sell")
				.addFormParameter("_ref_ck", _ref_ck);
		for (Stock stock : stocks) {
			req.addFormParameter(stock.getFormName(), stock.getShares());
		}
		return req.send();
	}
	
	private HttpResponse loadTicker(int id, String ticker) throws HttpHelperException {
		return helper.get("/stockmarket.phtml?type=buy&ticker=" + ticker)
				.addHeader(Headers.referer("http://www.neopets.com/stockmarket.phtml?type=profile&company_id=" + id))
				.send();
	}
	
	private HttpResponse loadProfile(int id) throws HttpHelperException {
		return helper.get("/stockmarket.phtml?type=profile&company_id=" + id)
				.addHeader(Headers.referer("http://www.neopets.com/stockmarket.phtml?type=list&full=true"))
				.send();
	}
	
	private HttpResponse loadPortfolio() throws HttpHelperException {
		return helper.get("/stockmarket.phtml?type=portfolio")
				.send();
	}
	
	private HttpResponse loadList() throws HttpHelperException {
		return helper.get("/stockmarket.phtml?type=list&full=true")
				.send();
	}
}
