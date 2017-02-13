package com.poncholay.EpictureSdk;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.poncholay.EpictureSdk.imgur.ImgurClient;

public class Main extends Activity {

	@Override
	protected void onCreate(Bundle ignored) {
		setContentView(R.layout.webview);

		super.onCreate(ignored);


		EpictureClient client = new ImgurClient.ImgurClientBuilder()
				.clientPrivate("e18f03df7f0e0bac37285b83f1b4264644d230d2")
				.clientId("3560cc6fe6a380b")
				.accessToken("93a2cf8f5fc17f00f344bdfb928c13e4c225cf30")
				.refreshToken("b6cfa8f64586b59bbd8578038bcc847ce1b43726")
				.build();

		client.authorize(this);
		client.me();
	}
}
