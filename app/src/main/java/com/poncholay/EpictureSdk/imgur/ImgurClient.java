package com.poncholay.EpictureSdk.imgur;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.poncholay.EpictureSdk.EpictureClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ImgurClient extends EpictureClient {

	private final String AUTHORIZE_URL = "https://api.imgur.com/oauth2/authorize?response_type=pin&client_id=3560cc6fe6a380b";
	private final String clientId;
	private final String clientSecret;
	private String accessToken;
	private String refreshToken;

	private ImgurClient(String clientPublic, String clientSecret, String accessToken, String refreshToken) {
		super("https://api.imgur.com/3/");
		this.clientId = clientPublic;
		this.clientSecret = clientSecret;
		setAccessToken(accessToken);
		setRefreshToken(refreshToken);
	}

	private void exchangePinForTokens(String pin) {
		RequestParams params = new RequestParams();
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("grant_type", "pin");
		params.add("pin", pin);
		this.postUrl("https://api.imgur.com/oauth2/token", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				try {
					if (response.has("access_token") && response.has("refresh_token")) {
						setAccessToken(response.getString("access_token"));
						setRefreshToken(response.getString("refresh_token"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				System.out.println(errorResponse);
			}
		});
	}

	public void authorize(Context context) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AUTHORIZE_URL));
		context.startActivity(browserIntent);

		new MaterialDialog.Builder(context)
				.title("Enter Pin")
				.positiveText("Validate")
				.negativeText("Cancel")
				.input("Pin", null, new MaterialDialog.InputCallback() {
					@Override
					public void onInput(@NonNull MaterialDialog dialog, @NonNull CharSequence input) {
						exchangePinForTokens(input.toString());
					}
				})
				.show();
	}

	public void me() {
		this.get("account/me", new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//				Gson gson = new GsonBuilder().create();
//				// Define Response class to correspond to the JSON response returned
//				gson.fromJson(responseString, Response.class);
				System.out.println(response);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				System.out.println(errorResponse);
			}
		});
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
		this.setAuthorizationHeader(accessToken);
	}

	private void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public static class ImgurClientBuilder {

		private String nestedClientPublic;
		private String nestedClientPrivate;
		private String nestedAccessToken;
		private String nestedRefreshToken;

		public ImgurClientBuilder() {}

		public ImgurClientBuilder clientId(String clientPublic) {
			this.nestedClientPublic = clientPublic;
			return this;
		}

		public ImgurClientBuilder clientPrivate(String clientPrivate) {
			this.nestedClientPrivate = clientPrivate;
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
			return new ImgurClient(nestedClientPublic, nestedClientPrivate, nestedAccessToken, nestedRefreshToken);
		}
	}
}