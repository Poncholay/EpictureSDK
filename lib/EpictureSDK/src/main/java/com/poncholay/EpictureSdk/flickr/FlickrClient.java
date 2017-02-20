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
import com.poncholay.EpictureSdk.model.response.EpictureCallbackInterface;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

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
	private static final boolean OAUTH = true;
	private static final boolean REGULAR = false;
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

	private String encodeUrl(String raw, boolean strict) {
		try {
			if (strict) {
				return URLEncoder.encode(raw.replace("+", "%20").replace("*", "%2A").replace("%7E", "~"), "UTF-8");
			}
			return URLEncoder.encode(raw, "UTF-8");
		} catch (UnsupportedEncodingException ignored) {}
		return raw;
	}


	private String getParamString(List<String> parameters) {
		StringBuilder buffer = new StringBuilder();

		java.util.Collections.sort(parameters);
		for (int i = 0; i < parameters.size(); i++) {
			buffer.append(parameters.get(i));
			if (i + 1 < parameters.size()) {
				buffer.append('&');
			}
		}
		return buffer.toString();
	}

	private List<String> getDefaultParam() {
		List<String> params = new ArrayList<>();
		params.add("oauth_timestamp=" + new Date().getTime());
		params.add("oauth_consumer_key=" + clientId);
		params.add("oauth_signature_method=HMAC-SHA1");
		params.add("oauth_version=1.0");
		params.add("perms=delete");
		return params;
	}

	private String getSignature(String verb, String url, List<String> parameters, String secret) {
		String params = getParamString(parameters);

		String rawSignature = verb + "&" + encodeUrl(url, OAUTH) + "&" + encodeUrl(params, OAUTH);
		String key = encodeUrl(clientSecret, OAUTH) + "&" + encodeUrl(secret, OAUTH);

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

	private void exchangePinForTokens(String pin, final EpictureCallbackInterface callback) {
		List<String> params = getDefaultParam();

		params.add("oauth_nonce=" + encodeUrl(getNonce(), REGULAR));
		params.add("oauth_verifier=" + pin);
		params.add("oauth_token=" + accessToken);

		String url = EXCHANGE_URL;
		url += "?" + getParamString(params);
		url += "&oauth_signature=" + encodeUrl(getSignature("GET", EXCHANGE_URL, params, privateToken), REGULAR);

		this.getUrl(url, new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				if (statusCode == 200) {
					if (parseResponseForParam(response, "oauth_token") != null && parseResponseForParam(response, "oauth_token_secret") != null) {
						setAccessToken(parseResponseForParam(response, "oauth_token"));
						setPrivateToken(parseResponseForParam(response, "oauth_token_secret"));

						if (callback != null) {
							EpictureAuthorization data = new FlickrAuthorization();
							data.setAccessToken(accessToken);
							data.setRefreshToken(privateToken);
							EpictureResponseWrapper<EpictureAuthorization> ret = new EpictureResponseWrapper<>(true, statusCode, data);
							callback.success(ret);
						}
						return;
					}
				}
				if (callback != null) {
					if (statusCode == 200) {
						callback.error(new EpictureResponseWrapper<>(false, statusCode, new FlickrError("Flickr responded oddly", "authorize")));
					} else {
						callback.error(new EpictureResponseWrapper<>(false, statusCode, new FlickrError(response, "authorize")));
					}
				}
			}
		});
	}

	private void authorizeToken(Context context, final EpictureCallbackInterface callback) {
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
	public void authorize(final Context context, final EpictureCallbackInterface callback) {
		List<String> params = getDefaultParam();

		params.add("oauth_nonce=" + encodeUrl(getNonce(), REGULAR));
		params.add("oauth_callback=oob");

		String url = REQUEST_URL;
		url += "?" + getParamString(params);
		url += "&oauth_signature=" + encodeUrl(getSignature("GET", REQUEST_URL, params, ""), REGULAR);

		this.getUrl(url, new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				if (statusCode == 200) {
					accessToken = parseResponseForParam(response, "oauth_token");
					privateToken = parseResponseForParam(response, "oauth_token_secret");
					if (accessToken == null || privateToken == null) {
						if (callback != null) {
							callback.error(new EpictureResponseWrapper<>(false, statusCode, new FlickrError("Flickr responded oddly", "authorize")));
						}
					}
//					authorizeToken(context, callback);
				} else {
					if (callback != null) {
						callback.error(new EpictureResponseWrapper<>(false, statusCode, new FlickrError(response, "authorize")));
					}
				}
			}
		});
	}

	@Override
	public void me(EpictureCallbackInterface callback) {
		if (callback != null) {
			callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Flickr responded oddly", "me")));
		}
	}

	@Override
	public void favorite(String id, EpictureCallbackInterface callback) {
		if (callback != null) {
			callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Flickr responded oddly", "favorite")));
		}
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

	@Override
	public String getServiceName() {
		return "Flickr";
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