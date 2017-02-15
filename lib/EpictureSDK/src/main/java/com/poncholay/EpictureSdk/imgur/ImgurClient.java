package com.poncholay.EpictureSdk.imgur;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.poncholay.EpictureSdk.EpictureClientAbstract;
import com.poncholay.EpictureSdk.imgur.model.ImgurAuthorization;
import com.poncholay.EpictureSdk.imgur.model.ImgurError;
import com.poncholay.EpictureSdk.imgur.model.ImgurUser;
import com.poncholay.EpictureSdk.model.EpictureAuthorization;
import com.poncholay.EpictureSdk.model.response.CallbackInterface;
import com.poncholay.EpictureSdk.model.response.ResponseWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ImgurClient extends EpictureClientAbstract {

	private final String AUTHORIZE_URL = "https://api.imgur.com/oauth2/authorize?response_type=pin&client_id=";
	private final String EXCHANGE_URL = "https://api.imgur.com/oauth2/token";
	private final String clientId;
	private final String clientSecret;
	private String accessToken;
	private String refreshToken;
	private Gson gson;

	private ImgurClient(String clientPublic, String clientSecret, String accessToken, String refreshToken) {
		super("https://api.imgur.com/3/");
		this.clientId = clientPublic;
		this.clientSecret = clientSecret;
		this.gson = new GsonBuilder().create();
		setAccessToken(accessToken);
		setRefreshToken(refreshToken);
	}

	private void exchangePinForTokens(String pin, final CallbackInterface callback) {
		RequestParams params = new RequestParams();
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("grant_type", "pin");
		params.add("pin", pin);
		this.postUrl(EXCHANGE_URL, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				try {
					if (response.has("access_token") && response.has("refresh_token")) {
						setAccessToken(response.getString("access_token"));
						setRefreshToken(response.getString("refresh_token"));

						EpictureAuthorization data = new ImgurAuthorization();
						data.setAccessToken(accessToken);
						data.setRefreshToken(refreshToken);
						ResponseWrapper<EpictureAuthorization> ret = new ResponseWrapper<>(true, statusCode, data);
						callback.success(ret);

						return;
					}
				} catch (JSONException ignored) {}
				callback.error(gson.fromJson(response.toString(), ImgurError.ImgurErrorWrapper.class));
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				callback.error(gson.fromJson(errorResponse.toString(), ImgurError.ImgurErrorWrapper.class));
			}
		});
	}

	@Override
	public void authorize(Context context, final CallbackInterface callback) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AUTHORIZE_URL + clientId));
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
	public void me(final CallbackInterface callback) {
		this.get("account/me", new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				callback.success(gson.fromJson(response.toString(), ImgurUser.ImgurUserWrapper.class));
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				callback.error(gson.fromJson(errorResponse.toString(), ImgurError.ImgurErrorWrapper.class));
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
		return refreshToken;
	}

	private void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
		this.setAuthorizationHeader(accessToken);
	}

	private void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public static class ImgurClientBuilder {

		private String nestedClientPublic;
		private String nestedClientSecret;
		private String nestedAccessToken;
		private String nestedRefreshToken;

		public ImgurClientBuilder() {}

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
			return new ImgurClient(nestedClientPublic, nestedClientSecret, nestedAccessToken, nestedRefreshToken);
		}
	}
}