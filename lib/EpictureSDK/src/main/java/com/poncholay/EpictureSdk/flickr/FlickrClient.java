package com.poncholay.EpictureSdk.flickr;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.poncholay.EpictureSdk.CallbackInterface;
import com.poncholay.EpictureSdk.EpictureClientAbstract;
import com.poncholay.EpictureSdk.imgur.model.ImgurError;
import com.poncholay.EpictureSdk.imgur.model.ImgurUser;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

import static com.poncholay.EpictureSdk.HmacSHA1.hmacSha1;

public class FlickrClient extends EpictureClientAbstract {
	private final String AUTHORIZE_URL = "https://www.flickr.com/services/oauth/request_token";
	private final String clientId;
	private final String clientSecret;
	private String accessToken;
	private String refreshToken;
	private Gson gson;

	private FlickrClient(String clientPublic, String clientSecret, String accessToken, String refreshToken) {
		super("https://api.flickr.com/services/rest");
		this.clientId = clientPublic;
		this.clientSecret = clientSecret;
		this.gson = new GsonBuilder().create();
		setAccessToken(accessToken);
		setRefreshToken(refreshToken);
	}

	private String encodeUrl(String raw) {
		try {
			return URLEncoder.encode(raw, "UTF-8");
		} catch (UnsupportedEncodingException ignored) {}
		return raw;
	}

	private String getParams(List<String> parameters) {
		String params = "";

		java.util.Collections.sort(parameters);
		for (int i = 0; i < parameters.size(); i++) {
			params += parameters.get(i);
			if (i + 1 < parameters.size()) {
				params += "&";
			}
		}
		return params;
	}

	private String getSignature(String verb, String url, List<String> parameters) {
		String params = getParams(parameters);

		String rawSignature = verb + "&" + encodeUrl(url) + "&" + encodeUrl(params);
		String key = clientSecret + "&";

		try {
			return hmacSha1(rawSignature, key);
		} catch (Exception e) {
			return "";
		}
	}

	private String getNonce() {
		return UUID.randomUUID().toString();
	}

	public void authorize(final Context context, final CallbackInterface callback) {
		List<String> params = new ArrayList<>();

		params.add("oauth_nonce=" + encodeUrl(getNonce()));
		params.add("oauth_timestamp=" + new Date().getTime());
		params.add("oauth_consumer_key=" + clientId);
		params.add("oauth_signature_method=HMAC-SHA1");
		params.add("oauth_version=1.0");
		params.add("oauth_callback=oob");
		this.getUrl(AUTHORIZE_URL + "?" + getParams(params) + "&oauth_signature=" + encodeUrl(getSignature("GET", AUTHORIZE_URL, params)), new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				if (statusCode == 200) {
					System.out.println("Success : " + response);
				} else {
					System.out.println("Error : " + response);
				}
			}
		});
	}

	public void me(CallbackInterface callback) {

	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	private void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	private void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public static class FlickrClientBuilder {

		private String nestedClientPublic;
		private String nestedClientPrivate;
		private String nestedAccessToken;
		private String nestedRefreshToken;

		public FlickrClientBuilder() {}

		public FlickrClientBuilder clientId(String clientPublic) {
			this.nestedClientPublic = clientPublic;
			return this;
		}

		public FlickrClientBuilder clientPrivate(String clientPrivate) {
			this.nestedClientPrivate = clientPrivate;
			return this;
		}

		public FlickrClientBuilder accessToken(String accessToken) {
			this.nestedAccessToken = accessToken;
			return this;
		}

		public FlickrClientBuilder refreshToken(String refreshToken) {
			this.nestedRefreshToken = refreshToken;
			return this;
		}

		public FlickrClient build() {
			return new FlickrClient(nestedClientPublic, nestedClientPrivate, nestedAccessToken, nestedRefreshToken);
		}
	}
}