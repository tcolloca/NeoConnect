package com.neopetsconnect.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class DomUtils {

	public static Element getContent(Document doc) {
		return doc.getElementById("content")
				.select(":root table > tbody > tr").first()
				.getElementsByClass("content").first();
	}
}
