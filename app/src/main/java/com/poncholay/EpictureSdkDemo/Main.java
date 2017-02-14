package com.poncholay.EpictureSdkDemo;

import android.app.Activity;
import android.os.Bundle;

import com.poncholay.EpictureSdk.CallbackInterface;
import com.poncholay.EpictureSdk.EpictureClientAbstract;
import com.poncholay.EpictureSdk.flickr.FlickrClient;
import com.poncholay.EpictureSdk.imgur.ImgurClient;
import com.poncholay.EpictureSdk.imgur.model.ImgurAuthorization;
import com.poncholay.EpictureSdk.imgur.model.ImgurError;
import com.poncholay.EpictureSdk.imgur.model.ImgurUser;
import com.poncholay.EpictureSdk.imgur.model.ImgurResponseWrapper;

public class Main extends Activity {

	private EpictureClientAbstract client;

	@Override
	protected void onCreate(Bundle ignored) {
		setContentView(R.layout.webview);

		super.onCreate(ignored);


		client = new ImgurClient.ImgurClientBuilder()
				.clientPrivate("e18f03df7f0e0bac37285b83f1b4264644d230d2")
				.clientId("3560cc6fe6a380b")
				.accessToken("8f23d10d32479a898bfa1c55103f091e09851a15")
				.refreshToken("b89661208037bea5168ef11c86db3ec28c475d94")
				.build();

//		doAuthorize();
		doMe();

		client = new FlickrClient.FlickrClientBuilder()
				.clientPrivate("80861547e3099611")
				.clientId("e00ba2ba9f093e1b4d777d24fd3c2d9f")
				.accessToken("")
				.refreshToken("")
				.build();

		doAuthorize();
		doMe();
	}

	private void doAuthorize() {
		client.authorize(this, new CallbackInterface<ImgurAuthorization>() {
			@Override
			public void success(ImgurResponseWrapper<ImgurAuthorization> response) {
				System.out.println("Access token : " + response.data.getAccessToken());
				doMe();
			}

			@Override
			public void error(ImgurResponseWrapper<ImgurError> error) {
				System.out.println(error.data.getPrettyError());
			}
		});
	}

	private void doMe() {
		client.me(new CallbackInterface<ImgurUser>() {
			@Override
			public void success(ImgurResponseWrapper<ImgurUser> response) {
				System.out.println("Id : " + response.data.getId());
			}

			@Override
			public void error(ImgurResponseWrapper<ImgurError> error) {
				System.out.println(error.data.getPrettyError());
			}
		});
	}
}