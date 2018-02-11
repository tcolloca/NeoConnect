package com.neopetsconnect.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpRequest;
import com.httphelper.main.HttpResponse;

public class HttpUtils {

  private HttpRequest req;
  private HttpResponse res;

  private HttpUtils() {}

  public static Element getContent(Document doc) {
    return doc.getElementById("content").select(":root table > tbody > tr").first()
        .getElementsByClass("content").first();
  }

  public static HttpUtils newInstance() {
    return new HttpUtils();
  }

  private static Document getDocument(HttpResponse res) {
    return Jsoup.parse(res.getContent().get());
  }

  public Document getDocument(HttpRequest req) {
    this.req = req;
    HttpResponse res = req.send();
    this.res = res;
    return getDocument(res);
  }

  public Element getContent(HttpRequest req) {
    return getContent(getDocument(req));
  }

  public void logRequestResponse() {
    HttpHelper.log(req);
    HttpHelper.log(res);
  }
}
