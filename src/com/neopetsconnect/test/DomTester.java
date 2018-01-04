package com.neopetsconnect.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.neopetsconnect.dailies.stocks.Stock;
import com.neopetsconnect.exceptions.ShopWizardBannedException;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.Utils;

public class DomTester implements ConfigProperties {

	public static void main(String[] args) throws IOException, ShopWizardBannedException {
		Document doc = Jsoup.parse(new File("test.html"), "UTF-8");
		Element form = doc.getElementById("postForm");
		String _ref_ck = form.select(":root > input[name=_ref_ck]").attr("value");
		System.out.println(_ref_ck);
		Elements tickerRows = form.select(":root > table > tbody > tr[style=display:none;] > td > table > tbody");
		double minChange = STOCK_MARKET_MIN_SELL;
		List<Stock> toSell = Utils.merge(tickerRows.stream().map(tickerElem -> {
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
		System.out.println(toSell);
	}
}	
