package com.poncholay.EpictureSdk.imgur;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.Base64;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.poncholay.EpictureSdk.EpictureClientAbstract;
import com.poncholay.EpictureSdk.imgur.model.ImgurAuthorization;
import com.poncholay.EpictureSdk.imgur.model.ImgurError;
import com.poncholay.EpictureSdk.imgur.model.ImgurPicture;
import com.poncholay.EpictureSdk.model.EpictureAuthorization;
import com.poncholay.EpictureSdk.model.response.EpictureCallbackInterface;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import cz.msebera.android.httpclient.Header;

public class ImgurClient extends EpictureClientAbstract {

	private final String AUTHORIZE_URL = "https://api.imgur.com/oauth2/authorize?response_type=pin&client_id=";
	private final String EXCHANGE_URL = "https://api.imgur.com/oauth2/token";
	private final String clientId;
	private final String clientSecret;
	private Activity activity;
	private String accessToken;
	private String refreshToken;
	private String username;
	private Gson gson;

	private ImgurClient(Activity activity, String clientPublic, String clientSecret, String accessToken, String refreshToken) {
		super("https://api.imgur.com/3/");
		this.clientId = clientPublic;
		this.clientSecret = clientSecret;
		this.gson = new GsonBuilder().create();
		this.activity = activity;

		SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
		this.username = prefs.getString("ImgurUsername", null);
		setAccessToken(accessToken == null ? prefs.getString("ImgurAccessToken", null) : accessToken);
		setRefreshToken(refreshToken == null ? prefs.getString("ImgurRefreshToken", null) : refreshToken);
	}

	private void exchangePinForTokens(String pin, final EpictureCallbackInterface callback) {
		RequestParams params = new RequestParams();
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("grant_type", "pin");
		params.add("pin", pin);
		this.postUrl(EXCHANGE_URL, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				try {
					if (response.has("access_token") && response.has("refresh_token") && response.has("account_username")) {
						setAccessToken(response.getString("access_token"));
						setRefreshToken(response.getString("refresh_token"));
						setUsername(response.getString("account_username"));

						if (callback != null) {
							EpictureAuthorization data = new ImgurAuthorization();
							data.setAccessToken(accessToken);
							data.setRefreshToken(refreshToken);
							callback.success(new EpictureResponseWrapper<>(true, statusCode, data));
						}

						return;
					}
				} catch (JSONException ignored) {}
				if (callback != null) {
					try {
						callback.error(gson.fromJson(response.toString(), ImgurError.ImgurErrorWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "authorize")));
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if (callback != null) {
					try {
						callback.error(gson.fromJson(errorResponse.toString(), ImgurError.ImgurErrorWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "authorize")));
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
	public void authorize(Context context, final EpictureCallbackInterface callback) {
		if (context == null) {
			return;
		}
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AUTHORIZE_URL + clientId));
		context.startActivity(browserIntent);
		pinValidator(context, callback);
	}

	private void callFavorite(final String id, final EpictureCallbackInterface callback, final boolean result) {
		this.post("image/" + id + "/favorite", new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				if (callback != null) {
					try {
						if (response.getString("data").equals("favorited") == result) {
							callback.success(new EpictureResponseWrapper<>(true, statusCode, result));
						} else {
							callFavorite(id, callback, result);
						}
					} catch (JSONException ignored) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "favoriteImage")));
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if (callback != null) {
					try {
						callback.error(gson.fromJson(errorResponse.toString(), ImgurError.ImgurErrorWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "favoriteImage")));
					}
				}
			}
		});
	}

	@Override
	public void unfavoriteImage(String id, EpictureCallbackInterface callback) {
		callFavorite(id, callback, false);
	}

	@Override
	public void favoriteImage(String id, EpictureCallbackInterface callback) {
		callFavorite(id, callback, true);
	}

	@Override
	public void getImages(EpictureCallbackInterface callback) {
		getImages(0, callback);
	}

	@Override
	public void getImages(String username, EpictureCallbackInterface callback) {
		if (username == null) {
			getImages(0, callback);
		} else {
			getImages(username, 0, callback);
		}
	}

	@Override
	public void getImages(int page, EpictureCallbackInterface callback) {
		if (this.username != null) {
			getImages(this.username, page, callback);
		} else if (callback != null) {
			callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not retrieve username", "getImages")));
		}
	}

	@Override
	public void getImages(String username, int page, final EpictureCallbackInterface callback) {
		this.get("/account/" + username + "/images/" + page, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				if (callback != null) {
					try {
						callback.success(gson.fromJson(response.toString(), ImgurPicture.ImgurPictureArrayWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "getImages")));
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if (callback != null) {
					try {
						callback.error(gson.fromJson(errorResponse.toString(), ImgurError.ImgurErrorWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "getImages")));
					}
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
		this.get("/account/" + username + "/favorites/" + page, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				if (callback != null) {
					try {
						callback.success(gson.fromJson(response.toString(), ImgurPicture.ImgurPictureArrayWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "getImages")));
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if (callback != null) {
					try {
						callback.error(gson.fromJson(errorResponse.toString(), ImgurError.ImgurErrorWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "getImages")));
					}
				}
			}
		});
	}

	private String buildSearchQuery(String search, String operator) {
		StringBuilder query = new StringBuilder();
		String[] params = search.split(" ");
		query.append("q_any=");
		boolean first = true;
		for (String param : params) {
			if (!first) {
				query.append(" ");
			} else {
				first = false;
			}
			query.append(param);
		}
		query.append("&q_type=png");
		return query.toString();
	}

	@Override
	public void searchMyImages(String search, EpictureCallbackInterface callback) {
		searchImages(search, callback);
		//TODO : implement it
	}

	@Override
	public void searchMyImages(String search, int page, EpictureCallbackInterface callback) {
		searchImages(search, page, callback);
		//TODO : implement it
	}

	@Override
	public void searchImages(String search, EpictureCallbackInterface callback) {
		searchImages(search, 0, callback);
	}

	@Override
	public void searchImages(String search, int page, final EpictureCallbackInterface callback) {
		if (search == null || search == "") {
			return;
		}
		String query = buildSearchQuery(search, "OR");
		this.get("/gallery/search/time/" + page + "?" + query, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				if (callback != null) {
					try {
						callback.success(gson.fromJson(response.toString(), ImgurPicture.ImgurPictureArrayWrapperEpicture.class));
					} catch (Exception e) {
						e.printStackTrace();
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "searchImages")));
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if (callback != null) {
					try {
						callback.error(gson.fromJson(errorResponse.toString(), ImgurError.ImgurErrorWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "searchImages")));
					}
				}
			}
		});
	}

	@Override
	public void getImage(String id, final EpictureCallbackInterface callback) {
		this.get("image/" + (id == null ? "" : id), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				if (callback != null) {
					try {
						callback.success(gson.fromJson(response.toString(), ImgurPicture.ImgurPictureWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "getImage")));
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if (callback != null) {
					try {
						callback.error(gson.fromJson(errorResponse.toString(), ImgurError.ImgurErrorWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "getImage")));
					}
				}
			}
		});
	}

	@Override
	public void uploadImage(String path, final EpictureCallbackInterface callback) {
		uploadImage(path, null, null, null, null, callback);
	}

	@Override
	public void uploadImage(String path, String album, String name, String title, String description, final EpictureCallbackInterface callback) {
		byte[] image = loadBinaryFile(path);
		if (image == null) {
			if (callback != null) {
				callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("The image does not exist", "getImage")));
			}
			return;
		}

		RequestParams params = new RequestParams();
		params.add("image", Base64.encodeToString(image, Base64.DEFAULT));
		params.add("album", album);
		params.add("type", "base64");
		params.add("name", name);
		params.add("title", title);
		params.add("description", description);
		this.post("/image", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				if (callback != null) {
					try {
						callback.success(gson.fromJson(response.toString(), ImgurPicture.ImgurPictureWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "getImage")));
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if (callback != null) {
					try {
						callback.error(gson.fromJson(errorResponse.toString(), ImgurError.ImgurErrorWrapperEpicture.class));
					} catch (Exception e) {
						callback.error(new EpictureResponseWrapper<>(false, 500, new ImgurError("Could not handle response", "getImage")));
					}
				}
			}
		});
	}

	private byte[] loadBinaryFile(String image) {
		DataInputStream dis = null;
		try {
			File file = new File(image);
			if (!file.exists()) {
				return null;
			}
			byte[] fileData = new byte[(int) file.length()];
			dis = new DataInputStream(new FileInputStream(file));
			dis.readFully(fileData);
			return fileData;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (dis != null) {
					dis.close();
				}
			} catch (Exception ignored) {}
		}
	}

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
		return refreshToken;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getServiceName() {
		return "Imgur";
	}

	private void storeInSharedPreferences(String key, String value) {
		SharedPreferences sharedPrefs = activity.getSharedPreferences("Epicture", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(key, value);
		editor.apply();
	}

	private void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
		this.setAuthorizationHeader(accessToken);
		storeInSharedPreferences("ImgurAccessToken", accessToken);
	}

	private void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
		storeInSharedPreferences("ImgurRefreshToken", refreshToken);
	}

	private void setUsername(String username) {
		this.username = username;
		storeInSharedPreferences("ImgurUsername", username);
	}

	public static class ImgurClientBuilder {

		private Activity nestedActivity;
		private String nestedClientPublic;
		private String nestedClientSecret;
		private String nestedAccessToken;
		private String nestedRefreshToken;

		public ImgurClientBuilder(Activity activity) {
			this.nestedActivity = activity;
		}

		public ImgurClientBuilder clientId(String clientPublic) {
			this.nestedClientPublic = clientPublic;
			return this;
		}

		public ImgurClientBuilder clientSecret(String clientSecret) {
			this.nestedClientSecret = clientSecret;
			return this;
		}

		public ImgurClientBuilder accessToken(String accessToken) {
			this.nestedAccessToken = accessToken;
			return this;
		}

		public ImgurClientBuilder refreshToken(String refreshToken) {
			this.nestedRefreshToken = refreshToken;
			return this;
		}

		public ImgurClient build() {
			return new ImgurClient(nestedActivity, nestedClientPublic, nestedClientSecret, nestedAccessToken, nestedRefreshToken);
		}
	}
}