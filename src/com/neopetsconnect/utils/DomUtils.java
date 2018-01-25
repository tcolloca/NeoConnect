package com.neopetsconnect.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.httphelper.main.HttpResponse;

public class DomUtils {

	public static Element getContent(HttpResponse resp) {
		return getContent(Jsoup.parse(resp.getContent().get()));
	}
	
	public static Element getContent(Document doc) {
		return doc.getElementById("content")
				.select(":root table > tbody > tr").first()
				.getElementsByClass("content").first();
	}
}
