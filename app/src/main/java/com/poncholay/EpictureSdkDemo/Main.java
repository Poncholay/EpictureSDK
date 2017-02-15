package com.poncholay.EpictureSdkDemo;

import android.app.Activity;
import android.os.Bundle;

import com.poncholay.EpictureSdk.model.EpictureUser;
import com.poncholay.EpictureSdk.model.response.CallbackInterface;
import com.poncholay.EpictureSdk.EpictureClientAbstract;
import com.poncholay.EpictureSdk.flickr.FlickrClient;
import com.poncholay.EpictureSdk.imgur.ImgurClient;
import com.poncholay.EpictureSdk.model.EpictureAuthorization;
import com.poncholay.EpictureSdk.model.EpictureError;
import com.poncholay.EpictureSdk.model.response.ResponseWrapper;

public class Main extends Activity {

	private EpictureClientAbstract client;

	@Override
	protected void onCreate(Bundle ignored) {
		setContentView(R.layout.webview);

		super.onCreate(ignored);


		client = new ImgurClient.ImgurClientBuilder()
				.clientSecret("e18f03df7f0e0bac37285b83f1b4264644d230d2")
				.clientId("3560cc6fe6a380b")
				.accessToken("f6a28e028df26540d431c1659289657baa085963")
				.refreshToken("497d08f26bafbed7a8e91bb2d862b0b89487c4d0")
				.build();

//		doAuthorize();
		doMe();

		client = new FlickrClient.FlickrClientBuilder()
				.clientSecret("80861547e3099611")
				.clientId("e00ba2ba9f093e1b4d777d24fd3c2d9f")
				.accessToken("72157676863555643-77d003c2604a4e58")
				.refreshToken("1bc9e607599835d0")
				.build();

//		doAuthorize();
		doMe();
	}

	private void doAuthorize() {
		client.authorize(this, new CallbackInterface<EpictureAuthorization>() {
			@Override
			public void success(ResponseWrapper<EpictureAuthorization> response) {
				System.out.println("Access token : " + response.data.getAccessToken());
				System.out.println("Refresh token : " + response.data.getRefreshToken());
				doMe();
			}

			@Override
			public void error(ResponseWrapper<EpictureError> error) {
				System.out.println(error.data.getPrettyError());
			}
		});
	}

	private void doMe() {
		client.me(new CallbackInterface<EpictureUser>() {
			@Override
			public void success(ResponseWrapper<EpictureUser> response) {
				System.out.println("Id : " + response.data.getId());
			}

			@Override
			public void error(ResponseWrapper<EpictureError> error) {
				System.out.println(error.data.getPrettyError());
			}
		});
	}
}

//	Flickr and Imgur API implementation
//		OK • Connecting to the Flickr and Imgur platforms
//		   • The photo display put online by the user connected to Flickr and Imgur
//		   • Flickr and Imgur photo finder
//		   • Uploading photos to Flickr and Imgur
//		   • Adding/deleting photos to/from your favorites
//		   • Managing photo display filters