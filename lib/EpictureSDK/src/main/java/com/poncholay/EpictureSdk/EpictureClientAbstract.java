package com.poncholay.EpictureSdk;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.LogInterface;
import com.loopj.android.http.RequestParams;
import com.poncholay.EpictureSdk.model.response.EpictureCallbackInterface;

import cz.msebera.android.httpclient.HttpEntity;

public abstract class EpictureClientAbstract {

	private final String URL;
	private final AsyncHttpClient client;

	protected EpictureClientAbstract(String URL) {
		this.URL = URL;
		this.client = new AsyncHttpClient();
		this.client.setLoggingLevel(LogInterface.VERBOSE);
	}

	abstract public String getClientId();
	abstract public String getClientSecret();
	abstract public String getAccessToken();
	abstract public String getRefreshToken();
	abstract public String getUsername();

	abstract public String getServiceName();

	abstract public void pinValidator(Context context, EpictureCallbackInterface callback);

	abstract public void authorize(Context context, EpictureCallbackInterface callback);

	abstract public void favoriteImage(String id, EpictureCallbackInterface callback);
	abstract public void unfavoriteImage(String id, EpictureCallbackInterface callback);

	abstract public void getFavoriteImages(EpictureCallbackInterface callback);
	abstract public void getFavoriteImages(int page, EpictureCallbackInterface callback);

	abstract public void getImages(EpictureCallbackInterface callback);
	abstract public void getImages(String username, EpictureCallbackInterface callback);
	abstract public void getImages(int page, EpictureCallbackInterface callback);
	abstract public void getImages(String username, int page, EpictureCallbackInterface callback);

	abstract public void searchImages(String search, EpictureCallbackInterface callback);
	abstract public void searchImages(String search, int page, EpictureCallbackInterface callback);
	abstract public void searchMyImages(String search, EpictureCallbackInterface callback);
	abstract public void searchMyImages(String search, int page, EpictureCallbackInterface callback);

	abstract public void getImage(String id, EpictureCallbackInterface callback);


	abstract public void uploadImage(String path, EpictureCallbackInterface callback);
	abstract public void uploadImage(String path, String album, String name, String title, String description, EpictureCallbackInterface callback);

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

	protected void postUrlWithEntity(String url, HttpEntity entity, AsyncHttpResponseHandler responseHandler) {
		client.post(null, url, entity, entity.getContentType().getValue(), responseHandler);
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

	protected String getBaseUrl() {
		return URL;
	}

	private String getAbsoluteUrl(String relativeUrl) {
		return URL + relativeUrl;
	}
}
