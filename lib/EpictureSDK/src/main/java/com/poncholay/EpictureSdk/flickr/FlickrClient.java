package com.poncholay.EpictureSdk.flickr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.poncholay.EpictureSdk.EpictureClientAbstract;
import com.poncholay.EpictureSdk.flickr.model.FlickrAuthorization;
import com.poncholay.EpictureSdk.flickr.model.FlickrError;
import com.poncholay.EpictureSdk.flickr.model.FlickrPicture;
import com.poncholay.EpictureSdk.model.EpictureAuthorization;
import com.poncholay.EpictureSdk.model.response.EpictureCallbackInterface;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

import static com.poncholay.EpictureSdk.utils.HmacSHA1.hmacSha1;

public class FlickrClient extends EpictureClientAbstract {
	private static final boolean OAUTH = true;
	private static final boolean REGULAR = false;
	private final String AUTHORIZE_URL = "https://www.flickr.com/services/oauth/authorize";
	private final String REQUEST_URL = "https://www.flickr.com/services/oauth/request_token";
	private final String EXCHANGE_URL = "https://www.flickr.com/services/oauth/access_token";
	private final String UPLOAD_URL = "https://up.flickr.com/services/upload/";
	private final String clientId;
	private final String clientSecret;
	private Activity activity;
	private String accessToken;
	private String privateToken;
	private String username;
	private Gson gson;

	private FlickrClient(Activity activity, String clientPublic, String clientSecret, String accessToken, String refreshToken) {
		super("https://api.flickr.com/services/rest");
		this.activity = activity;
		this.clientId = clientPublic;
		this.clientSecret = clientSecret;
		this.gson = new GsonBuilder().create();

		SharedPreferences prefs = activity.getSharedPreferences("Epicture", Context.MODE_PRIVATE);
		this.username = prefs.getString("FlickrUsername", null);
		setAccessToken(accessToken == null ? prefs.getString("FlickrAccessToken", null) : accessToken);
		setPrivateToken(refreshToken == null ? prefs.getString("FlickrRefreshToken", null) : refreshToken);
	}

	private JSONObject extractJSON(String str) {
		str = str.replaceFirst("jsonFlickrApi\\(", "");
		str = str.substring(0, str.length() - 1);
		try {
			return new JSONObject(str);
		} catch (JSONException e) {
			return null;
		}
	}

	private boolean handleResponseError(JSONObject response, EpictureCallbackInterface callback, String method) {
		try {
			if (response == null) {
				if (callback != null) {
					callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Flickr responded oddly", method)));
				}
				return false;
			}
			if ((response.has("stat") && response.getString("stat").equals("fail"))) {
				if ((response.has("message"))) {
					if (callback != null) {
						callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError(response.getString("message"), method)));
					}
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			if (callback != null) {
				callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Flickr responded oddly", method)));
			}
			return false;
		}
	}

	private String encodeUrl(String raw, boolean strict) {
		if (raw == null) {
			return "";
		}
		try {
			if (strict) {
				return URLEncoder.encode(raw.replace("+", "%20").replace("*", "%2A").replace("%7E", "~"), "UTF-8");
			}
			return URLEncoder.encode(raw, "UTF-8");
		} catch (UnsupportedEncodingException ignored) {}
		return raw;
	}

	private String getParamString(TreeMap<String, String> parameters) {
		StringBuilder buffer = new StringBuilder();

		boolean first = true;
		for (Map.Entry<String, String> bundle : parameters.entrySet()) {
			if (!first) {
				buffer.append('&');
			} else {
				first = false;
			}
			buffer.append(bundle.getKey());
			buffer.append('=');
			buffer.append(bundle.getValue());
		}
		return buffer.toString();
	}

	private TreeMap<String, String> getDefaultAuthParam() {
		TreeMap<String, String> params = new TreeMap<>();
		params.put("oauth_timestamp", String.valueOf(new Date().getTime()));
		params.put("oauth_consumer_key", clientId);
		params.put("oauth_signature_method", "HMAC-SHA1");
		params.put("oauth_version", "1.0");
		return params;
	}

	private String getSignature(String verb, String url, TreeMap<String, String> parameters, String secret) {
		String params = getParamString(parameters);

		String rawSignature = verb + "&" + encodeUrl(url, OAUTH) + "&" + encodeUrl(params, OAUTH);
		String key = encodeUrl(clientSecret, OAUTH) + "&" + encodeUrl(secret, OAUTH);

		try {
			String ret = hmacSha1(rawSignature, key);

			if (ret.contains("+")) {
				/*
				** TODO : find a more elegant solution
				** Cannot figure the signature encoding when it contains a '+' so generate another
				*/
				parameters.put("oauth_nonce", encodeUrl(generateNonce(), REGULAR));
				parameters.put("oauth_timestamp", String.valueOf(new Date().getTime()));
				return getSignature(verb, url, parameters, secret);
			}
			return ret;
		} catch (Exception e) {
			return "";
		}
	}

	private String generateNonce() {
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
		TreeMap<String, String> params = getDefaultAuthParam();

		params.put("oauth_nonce", encodeUrl(generateNonce(), REGULAR));
		params.put("oauth_verifier", pin);
		params.put("oauth_token", accessToken);

		String signature = encodeUrl(getSignature("GET", EXCHANGE_URL, params, privateToken), REGULAR);
		String url = EXCHANGE_URL;
		url += "?" + getParamString(params);
		url += "&oauth_signature=" + signature;
		this.getUrl(url, new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//AccessToken response always registers as failure so we treat it here
				if (statusCode == 200) {
					if (parseResponseForParam(response, "oauth_token") != null && parseResponseForParam(response, "oauth_token_secret") != null) {

						setAccessToken(parseResponseForParam(response, "oauth_token"));
						setPrivateToken(parseResponseForParam(response, "oauth_token_secret"));
						setUsername(parseResponseForParam(response, "username"));

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

	@Override
	public void pinValidator(Context context, final EpictureCallbackInterface callback) {
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
		TreeMap<String, String> params = getDefaultAuthParam();

		params.put("oauth_nonce", encodeUrl(generateNonce(), REGULAR));
		params.put("oauth_callback", "oob");

		String signature = encodeUrl(getSignature("GET", REQUEST_URL, params, ""), REGULAR);
		String url = REQUEST_URL;
		url += "?" + getParamString(params);
		url += "&oauth_signature=" + signature;

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
							return;
						}
					}
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AUTHORIZE_URL + "?oauth_token=" + accessToken + "&perms=delete"));
					context.startActivity(browserIntent);
					pinValidator(context, callback);
				} else {
					if (callback != null) {
						callback.error(new EpictureResponseWrapper<>(false, statusCode, new FlickrError(response, "authorize")));
					}
				}
			}
		});
	}

	@Override
	public void favoriteImage(final String id, final EpictureCallbackInterface callback) {
		callFavorite(id, callback, true);
	}

	@Override
	public void unfavoriteImage(String id, EpictureCallbackInterface callback) {
		callFavorite(id, callback, false);
	}

	private void callFavorite(String id, final EpictureCallbackInterface callback, final boolean favorite) {
		TreeMap<String, String> params = getDefaultAuthParam();

		params.put("oauth_nonce", encodeUrl(generateNonce(), REGULAR));
		params.put("oauth_token", encodeUrl(accessToken, REGULAR));
		params.put("api_key", clientId);
		params.put("photo_id", id);
		params.put("format", "json");
		params.put("method", encodeUrl("flickr.favorites." + (favorite ? "add" : "remove"), REGULAR));

		String signature = encodeUrl(getSignature("GET", getBaseUrl(), params, privateToken), REGULAR);
		String url = "?" + getParamString(params);
		url += "&oauth_signature=" + signature;

		this.get(url, new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				try {
					if (statusCode == 200) {
						JSONObject resp = extractJSON(response);
						if (handleResponseError(resp, callback, (favorite ? "" : "un") + "favoriteImage")) {
							callback.success(new EpictureResponseWrapper<>(true, 200, favorite));
						}
					} else {
						callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError(response, (favorite ? "" : "un") + "favoriteImage")));
					}
				} catch (Exception e) {
					callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Flickr responded oddly", (favorite ? "" : "un") + "favoriteImage")));
				}
			}
		});
	}

	@Override
	public void getImage(final String id, final EpictureCallbackInterface callback) {
		TreeMap<String, String> params = getDefaultAuthParam();

		params.put("api_key", clientId);
		params.put("photo_id", id);
		params.put("format", "json");
		params.put("method", "flickr.photos.getSizes");

		String signature = encodeUrl(getSignature("GET", getBaseUrl(), params, privateToken), REGULAR);
		String url = "?" + getParamString(params);
		url += "&oauth_signature=" + signature;

		this.get(url, new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				try {
					if (statusCode == 200) {
						JSONObject resp = extractJSON(response);
						if (handleResponseError(resp, callback, "getImage")) {
							JSONObject data = new JSONObject();
							JSONObject image = new JSONObject();
							image.put("title", id.toString());
							image.put("id", id);
							image.put("url_o", "");
							image.put("url_o", "");
							JSONArray sizes = resp.getJSONObject("sizes").getJSONArray("size");
							for (int i = 0; i < sizes.length(); i++) {
								if (((JSONObject)(sizes.get(i))).get("label").equals("Original")) {
									image.put("url_o", ((JSONObject)(sizes.get(i))).get("source"));
								}
								if (((JSONObject)(sizes.get(i))).get("label").equals("Small")) {
									image.put("url_m", ((JSONObject)(sizes.get(i))).get("source"));
								}
							}
							data.put("data", image);
							data.put("status", 200);
							data.put("success", true);
							callback.success(gson.fromJson(data.toString(), FlickrPicture.FlickrPictureWrapperEpicture.class));
						} else {
							callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError(response, "getImage")));
						}
					}
				} catch (Exception e) {
					callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Flickr responded oddly", "getImage")));
				}
			}
		});
	}

	@Override
	public void getImages(final EpictureCallbackInterface callback) {
		getImages(0, callback);
	}

	@Override
	public void getImages(String username, final EpictureCallbackInterface callback) {
		if (username == null) {
			getImages(0, callback);
		} else {
			getImages(username, 0, callback);
		}
	}

	@Override
	public void getImages(final int page, final EpictureCallbackInterface callback) {
		if (this.username != null) {
			getImages(this.username, page, callback);
		} else if (callback != null) {
			callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Could not retrieve username", "getImages")));
		}
	}

	@Override
	public void getImages(String username, final int page, final EpictureCallbackInterface callback) {
		TreeMap<String, String> params = new TreeMap<>();

		params.put("api_key", clientId);
		params.put("username", username);
		params.put("format", "json");
		params.put("method", "flickr.people.findByUsername");

		String url = "?" + getParamString(params);
		this.get(url, new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				try {
					if (statusCode == 200) {
						JSONObject resp = extractJSON(response);
						if (handleResponseError(resp, callback, "getImages")) {
							getImagesWithUserId(resp.getJSONObject("user").getString("id"), page, callback);
						}
					} else {
						callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError(response, "getImages")));
					}
				} catch (Exception e) {
					callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Flickr responded oddly", "getImages")));
				}
			}
		});
	}

	@Override
	public void getFavoriteImages(final EpictureCallbackInterface callback) {
		getFavoriteImages(0, callback);
	}

	@Override
	public void getFavoriteImages(int page, final EpictureCallbackInterface callback) {
		TreeMap<String, String> params = getDefaultAuthParam();

		params.put("oauth_nonce", encodeUrl(generateNonce(), REGULAR));
		params.put("oauth_token", accessToken);
		params.put("api_key", clientId);
		params.put("page", String.valueOf(page));
		params.put("extras", "url_o,url_m");
		params.put("format", "json");
		params.put("method", "flickr.favorites.getList");

		String signature = encodeUrl(getSignature("GET", getBaseUrl(), params, privateToken), REGULAR);
		String url = "?" + getParamString(params);
		url += "&oauth_signature=" + signature;

		this.get(url, new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				try {
					if (statusCode == 200) {
						JSONObject resp = extractJSON(response);
						if (handleResponseError(resp, callback, "getFavoriteImages")) {
							JSONObject data = new JSONObject();
							data.put("data", resp.getJSONObject("photos").getJSONArray("photo"));
							data.put("success", true);
							data.put("status", 200);
							callback.success(gson.fromJson(data.toString(), FlickrPicture.FlickrPictureArrayWrapperEpicture.class));
						}
					} else {
						callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError(response, "getFavoriteImages")));
					}
				} catch (Exception e) {
					callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Flickr responded oddly", "getImages")));
				}

			}
		});
	}

	private void getImagesWithUserId(String userId, final int page, final EpictureCallbackInterface callback) {
		TreeMap<String, String> params = getDefaultAuthParam();

		params.put("api_key", clientId);
		params.put("user_id", userId);
		params.put("page", String.valueOf(page));
		params.put("extras", "url_o,url_m");
		params.put("format", "json");
		params.put("method", "flickr.people.getPhotos");

		String signature = encodeUrl(getSignature("GET", getBaseUrl(), params, privateToken), REGULAR);
		String url = "?" + getParamString(params);
		url += "&oauth_signature=" + signature;

		this.get(url, new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				try {
					if (statusCode == 200) {
						JSONObject resp = extractJSON(response);
						if (handleResponseError(resp, callback, "getImages")) {
							JSONObject data = new JSONObject();
							data.put("data", resp.getJSONObject("photos").getJSONArray("photo"));
							data.put("success", true);
							data.put("status", 200);
							callback.success(gson.fromJson(data.toString(), FlickrPicture.FlickrPictureArrayWrapperEpicture.class));
						}
					} else {
						callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError(response, "getImages")));
					}
				} catch (Exception e) {
					callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Flickr responded oddly", "getImages")));
				}

			}
		});
	}

	@Override
	public void searchMyImages(String search, EpictureCallbackInterface callback) {
		searchImagesWithParam(search, 0, callback, true);
	}

	@Override
	public void searchMyImages(String search, int page, EpictureCallbackInterface callback) {
		searchImagesWithParam(search, page, callback, true);
	}

	@Override
	public void searchImages(String search, EpictureCallbackInterface callback) {
		searchImagesWithParam(search, 0, callback, false);
	}

	@Override
	public void searchImages(String search, int page, final EpictureCallbackInterface callback) {
		searchImagesWithParam(search, page, callback, false);
	}

	private void searchImagesWithParam(String search, int page, final EpictureCallbackInterface callback, boolean me) {
		TreeMap<String, String> params = getDefaultAuthParam();

		params.put("api_key", clientId);
		params.put("page", String.valueOf(page));
		params.put("extras", "url_o,url_m");
		params.put("format", "json");
		params.put("method", "flickr.photos.search");
		params.put("text", search);
		if (me) {
			params.put("user_id", "me");
		}

		String signature = encodeUrl(getSignature("GET", getBaseUrl(), params, privateToken), REGULAR);
		String url = "?" + getParamString(params);
		url += "&oauth_signature=" + signature;

		get(url, new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				try {
					if (statusCode == 200) {
						JSONObject resp = extractJSON(response);
						if (handleResponseError(resp, callback, "searchImages")) {
							JSONObject data = new JSONObject();
							data.put("data", resp.getJSONObject("photos").getJSONArray("photo"));
							data.put("success", true);
							data.put("status", 200);
							callback.success(gson.fromJson(data.toString(), FlickrPicture.FlickrPictureArrayWrapperEpicture.class));
						}
					} else {
						callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError(response, "searchImages")));
					}
				} catch (Exception e) {
					callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Flickr responded oddly", "searchImages")));
				}
			}
		});
	}

	@Override
	public void uploadImage(String path, EpictureCallbackInterface callback) {
		uploadImage(path, null, null, null, null, callback);
	}

	@Override
	public void uploadImage(String path, String album, String name, String title, String description, final EpictureCallbackInterface callback) {
		String[] split = path.split("\\.");
		String filename;
		if (split.length >= 2) {
			filename = split[split.length - 1] + "." + split[split.length - 2];
		} else if (split.length == 1) {
			filename = split[split.length - 1];
		} else {
			callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Bad file", "uploadImage")));
			return;
		}

		TreeMap<String, String> params = new TreeMap<>();

		params.put("oauth_timestamp", String.valueOf(new Date().getTime()));
		params.put("oauth_nonce", encodeUrl(generateNonce(), REGULAR));
		params.put("oauth_token", encodeUrl(accessToken, REGULAR));
		params.put("oauth_consumer_key", clientId);
		params.put("oauth_signature_method", "HMAC-SHA1");
		params.put("oauth_version", "1.0");
//		params.put("format", "json");
//		params.put("title", encodeUrl(title, REGULAR));
//		params.put("description", encodeUrl(description, REGULAR));

		String signature = encodeUrl(getSignature("POST", UPLOAD_URL, params, privateToken), REGULAR);

		RequestParams parameters = new RequestParams(params);
		try {
			parameters.put("photo", new File(path), filename);
		} catch (FileNotFoundException e) {
			callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Bad file", "uploadImage")));
			return;
		}
		String url = UPLOAD_URL;
		url += "?oauth_signature=" + signature;

		postUrl(url, parameters, new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
				//RequestToken response always registers as failure so we treat it here
				try {
					if (statusCode == 200) {
						JSONObject resp = extractJSON(response);
						if (handleResponseError(resp, callback, "uploadImage")) {
							JSONObject data = new JSONObject();
							data.put("data", new FlickrPicture());
							data.put("success", true);
							data.put("status", 200);
							callback.success(gson.fromJson(data.toString(), FlickrPicture.FlickrPictureWrapperEpicture.class));
						}
					} else {
						callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError(response, "uploadImages")));
					}
				} catch (Exception e) {
					callback.error(new EpictureResponseWrapper<>(false, 42, new FlickrError("Flickr responded oddly", "uploadImage")));
				}

			}
		});
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
	public String getUsername() {
		return username;
	}

	@Override
	public String getServiceName() {
		return "Flickr";
	}

	private void storeInSharedPreferences(String key, String value) {
		SharedPreferences sharedPrefs = activity.getSharedPreferences("Epicture", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(key, value);
		editor.apply();
	}

	private void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
		storeInSharedPreferences("FlickrAccessToken", accessToken);
	}

	private void setPrivateToken(String privateToken) {
		this.privateToken = privateToken;
		storeInSharedPreferences("FlickrRefreshToken", privateToken);
	}

	private void setUsername(String username) {
		this.username = username;
		storeInSharedPreferences("FlickrUsername", username);
	}

	public static class FlickrClientBuilder {

		private Activity nestedContext;
		private String nestedClientPublic;
		private String nestedClientSecret;
		private String nestedAccessToken;
		private String nestedRefreshToken;

		public FlickrClientBuilder(Activity context) {
			this.nestedContext = context;
		}

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
			return new FlickrClient(nestedContext, nestedClientPublic, nestedClientSecret, nestedAccessToken, nestedRefreshToken);
		}
	}
}