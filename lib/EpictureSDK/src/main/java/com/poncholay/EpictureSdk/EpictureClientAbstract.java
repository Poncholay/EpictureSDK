package com.poncholay.EpictureSdk;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.poncholay.EpictureSdk.model.response.EpictureCallbackInterface;

public abstract class EpictureClientAbstract {

	private final String URL;
	private final AsyncHttpClient client;

	protected EpictureClientAbstract(String URL) {
		this.URL = URL;
		this.client = new AsyncHttpClient();
	}

	abstract public String getClientId();
	abstract public String getClientSecret();
	abstract public String getAccessToken();
	abstract public String getRefreshToken();

	abstract public void authorize(Context context, EpictureCallbackInterface callback);
	abstract public void me(EpictureCallbackInterface callback);

	protected void setAuthorizationHeader(String accessToken) {
		client.removeHeader("Authorization:");
		client.addHeader("Authorization:", "Bearer " + accessToken);
	}

	protected void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	protected void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}

	protected void getUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.get(url, params, responseHandler);
	}

	protected void postUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.post(url, params, responseHandler);
	}

	protected void get(String url, AsyncHttpResponseHandler responseHandler) {
		client.get(getAbsoluteUrl(url), null, responseHandler);
	}

	protected void post(String url, AsyncHttpResponseHandler responseHandler) {
		client.post(getAbsoluteUrl(url), null, responseHandler);
	}

	protected void getUrl(String url, AsyncHttpResponseHandler responseHandler) {
		client.get(url, null, responseHandler);
	}

	protected void postUrl(String url, AsyncHttpResponseHandler responseHandler) {
		client.post(url, null, responseHandler);
	}

	private String getAbsoluteUrl(String relativeUrl) {
		return URL + relativeUrl;
	}
}
