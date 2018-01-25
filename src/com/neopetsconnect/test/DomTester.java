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
import com.neopetsconnect.exceptions.FaerieQuestException;
import com.neopetsconnect.exceptions.ShopWizardBannedException;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.DomUtils;
import com.neopetsconnect.utils.Utils;

public class DomTester implements ConfigProperties {

	public static void main(String[] args) throws IOException, ShopWizardBannedException, FaerieQuestException {
		Document doc = Jsoup.parse(new File("test.html"), "UTF-8");
		Element content = DomUtils.getContent(doc);
		Element shopWizContent = content.select(":root > div").get(1);
		Elements tables = shopWizContent.select(":root > table");
		if (tables.isEmpty()) {
			Elements centers = shopWizContent.select(":root > center");
			if (centers.isEmpty()) {
				Elements ps = shopWizContent.select(":root > p");
				if (!ps.isEmpty()) {
					String text = ps.first().text();					
					throw new FaerieQuestException(text); 
				}
			} else {
				String text = centers.select(":root > p").first().text();
				throw new ShopWizardBannedException(text);
			}
		}
	}
}	
