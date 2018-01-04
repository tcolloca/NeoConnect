package com.neopetsconnect.main;

import com.httphelper.main.Headers;
import com.httphelper.main.HttpContentType;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpResponse;

public class Session {

	private final HttpHelper helper;
	
	public Session(HttpHelper helper) {
		this.helper = helper;
	}
	
	public HttpResponse login(String username, String password) throws HttpHelperException {
		return helper.post("/login.phtml")
			.addFormParameter("destination", "%252Findex.phtml")
			.addFormParameter("username", username)
			.addFormParameter("password", password)
			.addHeader(Headers.referer("http://www.neopets.com/index.phtml"))
			.addHeader(Headers.contentType(HttpContentType.APP_X_WWW_FORM_ENCODED))
			.send();
	}
	
	public void logout() throws HttpHelperException {
		helper.clone("nc.neopets.com")
			.setDefaultHost("nc.neopets.com")
			.get("/auth/logout")
			.addQueryParameter("no-redirect", "1")
			.addHeader(Headers.referer("http://www.neopets.com/index.phtml"))
			.send();
		
		helper.get("/logout.phtml")
			.addHeader(Headers.referer("http://www.neopets.com/index.phtml"))
			.send();
	}
}
