package com.neopetsconnect.main;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpResponse;

public class Session {

  private final HttpHelper helper;
  private final String username;
  private final String password;
  private final String pin;

  public Session(HttpHelper helper, String username, String password, String pin) {
    this.helper = helper;
    this.username = username;
    this.password = password;
    this.pin = pin;
  }

  public HttpResponse login() {
    return helper.post("/login.phtml").addFormParameter("destination", "%252Findex.phtml")
        .addFormParameter("username", username).addFormParameter("password", password)
        .addHeader(Headers.referer("http://www.neopets.com/index.phtml"))
        .addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED)).send();
  }

  public void logout() {
    helper.clone("nc.neopets.com").setDefaultHost("nc.neopets.com").get("/auth/logout")
        .addQueryParameter("no-redirect", "1")
        .addHeader(Headers.referer("http://www.neopets.com/index.phtml")).send();

    helper.get("/logout.phtml").addHeader(Headers.referer("http://www.neopets.com/index.phtml"))
        .send();
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getPin() {
    return pin;
  }

  public boolean usingPin() {
    return pin != null && pin != "";
  }
}
