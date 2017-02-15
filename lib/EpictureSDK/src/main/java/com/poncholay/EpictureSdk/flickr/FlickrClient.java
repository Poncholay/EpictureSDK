package com.poncholay.EpictureSdk.flickr;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.poncholay.EpictureSdk.EpictureClientAbstract;
import com.poncholay.EpictureSdk.flickr.model.FlickrAuthorization;
import com.poncholay.EpictureSdk.flickr.model.FlickrError;
import com.poncholay.EpictureSdk.model.EpictureAuthorization;
import com.poncholay.EpictureSdk.model.EpictureError;
import com.poncholay.EpictureSdk.model.response.CallbackInterface;
import com.poncholay.EpictureSdk.model.response.ResponseWrapper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

import static com.poncholay.EpictureSdk.utils.HmacSHA1.hmacSha1;

public class FlickrClient extends EpictureClientAbstract {
	private final String AUTHORIZE_URL = "https://www.flickr.com/services/oauth/authorize";
	private final String REQUEST_URL = "https://www.flickr.com/services/oauth/request_token";
	private final String EXCHANGE_URL = "https://www.flickr.com/services/oauth/access_token";
	private final String clientId;
	private final String clientSecret;
	private String accessToken;
	private String privateToken;
	private Gson gson;

	private FlickrClient(String clientPublic, String clientSecret, String accessToken, String refreshToken) {
		super("https://api.flickr.com/services/rest");
		this.clientId = clientPublic;
		this.clientSecret = clientSecret;
		this.gson = new GsonBuilder().create();
		setAccessToken(accessToken);
		setPrivateToken(refreshToken);
	}

	private String encodeUrl(String raw) {
		try {
			return URLEncoder.encode(raw.replace("+", "%2b"), "UTF-8");
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

	private List<String> getDefaultParam() {
		List<String> params = new ArrayList<>();
		params.add("oauth_timestamp=" + new Date().getTime());
		params.add("oauth_consumer_key=" + clientId);
		params.add("oauth_signature_method=HMAC-SHA1");
		params.add("oauth_version=1.0");
		return params;
	}

	private String getSignature(String verb, String url, List<String> parameters, String secret) {
		String params = getParams(parameters);

		String rawSignature = verb + "&" + encodeUrl(url) + "&" + encodeUrl(params);
		String key = clientSecret + "&" + secret;

		try {
			return hmacSha1(rawSignature, key);
		} catch (Exception e) {
			return "";
		}
	}

	private String getNonce() {
		return UUID.randomUUID().toString();
	}

	private String parseResponseForParam(String response, String keyToFind) {
		String[] params = response.split("&");

		for (String param : params) {
			String[] parts = param.split("=");
			if (parts.length == 2) {
				String key = parts[0];
				String value = parts[1];
				if (Objects.equals(key, keyToFind)) {
					return value;
				}
			}
		}
		return null;
	}

	private void exchangePinForTokens(String pin, final CallbackInterface callback) {
		List<String> params = getDefaultParam();

		params.add("oauth_nonce=" + encodeUrl(getNonce()));
		params.add("oauth_verifier=" + pin);
		params.add("oauth_token=" + accessToken);
		this.getUrl(EXCHANGE_URL + "?" + getParams(params) + "&oauth_signature=" + encodeUrl(getSignature("GET", EXCHANGE_URL, params, privateToken)), new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				if (statusCode == 200) {
					if (parseResponseForParam(response, "oauth_token") != null && parseResponseForParam(response, "oauth_token_secret") != null) {
						setAccessToken(parseResponseForParam(response, "oauth_token"));
						setPrivateToken(parseResponseForParam(response, "oauth_token_secret"));

						EpictureAuthorization data = new FlickrAuthorization();
						data.setAccessToken(accessToken);
						data.setRefreshToken(privateToken);
						ResponseWrapper<EpictureAuthorization> ret = new ResponseWrapper<>(true, statusCode, data);
						callback.success(ret);

						return;
					}
					EpictureError data = new FlickrError();
					data.setError("Flickr responded oddly");
					callback.error(new ResponseWrapper<>(true, statusCode, data));
				} else {
					EpictureError data = new FlickrError();
					data.setError(response);
					callback.error(new ResponseWrapper<>(true, statusCode, data));
				}
			}
		});
	}

	private void authorizeToken(Context context, final CallbackInterface callback) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AUTHORIZE_URL + "?oauth_token=" + accessToken));
		context.startActivity(browserIntent);

		new MaterialDialog.Builder(context)
				.title("Enter Pin")
				.positiveText("Validate")
				.negativeText("Cancel")
				.input("Pin", null, new MaterialDialog.InputCallback() {
					@Override
					public void onInput(@NonNull MaterialDialog dialog, @NonNull CharSequence input) {
						exchangePinForTokens(input.toString(), callback);
					}
				})
				.show();
	}

	@Override
	public void authorize(final Context context, final CallbackInterface callback) {
		List<String> params = getDefaultParam();

		params.add("oauth_nonce=" + encodeUrl(getNonce()));
		params.add("oauth_callback=oob");
		this.getUrl(REQUEST_URL + "?" + getParams(params) + "&oauth_signature=" + encodeUrl(getSignature("GET", REQUEST_URL, params, "")), new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				if (statusCode == 200) {
					accessToken = parseResponseForParam(response, "oauth_token");
					privateToken = parseResponseForParam(response, "oauth_token_secret");
					if (accessToken == null || privateToken == null) {
						EpictureError data = new FlickrError();
						data.setError("Flickr responded oddly");
						callback.error(new ResponseWrapper<>(true, statusCode, data));
					}
					authorizeToken(context, callback);
				} else {
					EpictureError data = new FlickrError();
					data.setError(response);
					callback.error(new ResponseWrapper<>(true, statusCode, data));
				}
			}
		});
	}

	@Override
	public void me(CallbackInterface callback) {

	}

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public String getClientSecret() {
		return clientSecret;
	}

	@Override
	public String getAccessToken() {
		return accessToken;
	}

	@Override
	public String getRefreshToken() {
		return privateToken;
	}

	private void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	private void setPrivateToken(String privateToken) {
		this.privateToken = privateToken;
	}

	public static class FlickrClientBuilder {

		private String nestedClientPublic;
		private String nestedClientSecret;
		private String nestedAccessToken;
		private String nestedRefreshToken;

		public FlickrClientBuilder() {}

		public FlickrClientBuilder clientId(String clientPublic) {
			this.nestedClientPublic = clientPublic;
			return this;
		}

		public FlickrClientBuilder clientSecret(String clientSecret) {
			this.nestedClientSecret = clientSecret;
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
			return new FlickrClient(nestedClientPublic, nestedClientSecret, nestedAccessToken, nestedRefreshToken);
		}
	}
}