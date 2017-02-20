package com.poncholay.EpictureSdkDemo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.nightonke.boommenu.Util;
import com.poncholay.EpictureSdk.EpictureClientAbstract;
import com.poncholay.EpictureSdk.flickr.FlickrClient;
import com.poncholay.EpictureSdk.imgur.ImgurClient;
import com.poncholay.EpictureSdk.model.EpictureAuthorization;
import com.poncholay.EpictureSdk.model.EpictureError;
import com.poncholay.EpictureSdk.model.EpicturePicture;
import com.poncholay.EpictureSdk.model.EpictureUser;
import com.poncholay.EpictureSdk.model.response.EpictureCallbackInterface;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

import java.util.ArrayList;

public class Main extends Activity {

	private static final String TAG = "Main";

	private ArrayAdapter<String> mAdapter;

	@Override
	protected void onCreate(Bundle ignored) {
		super.onCreate(ignored);

		setContentView(R.layout.sample_view);

		ListView mListView = (ListView) findViewById(R.id.list_view);
		mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
		mListView.setAdapter(mAdapter);

		createBoomMenu();
	}

	private void createBoomMenu() {
		BoomMenuButton bmb = (BoomMenuButton) findViewById(R.id.bmb);
		bmb.setButtonEnum(ButtonEnum.Ham);
		bmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_5);
		bmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_5);

		Drawable clear = ContextCompat.getDrawable(this, R.drawable.clear);
		Drawable imgur = ContextCompat.getDrawable(this, R.drawable.imgur);
		Drawable flickr = ContextCompat.getDrawable(this, R.drawable.flickr);

		bmb.addBuilder(createHamButton(ColorUtils.darken(Color.GRAY), "Clear", "Remove log message", clear, new OnBMClickListener() {
			@Override
			public void onBoomButtonClick(int index) {
				mAdapter.clear();
				mAdapter.notifyDataSetChanged();
			}
		}));
		bmb.addBuilder(createHamButton(ColorUtils.darken(Color.RED), "Test Flickr", "Without authorization", flickr, new OnBMClickListener() {
			@Override
			public void onBoomButtonClick(int index) {
				EpictureClientAbstract client = new FlickrClient.FlickrClientBuilder()
						.clientSecret("80861547e3099611")
						.clientId("e00ba2ba9f093e1b4d777d24fd3c2d9f")
						.accessToken("72157676863555643-77d003c2604a4e58")
						.refreshToken("1bc9e607599835d0")
						.build();
				doTests(client);
			}
		}));
		bmb.addBuilder(createHamButton(ColorUtils.darken(Color.RED), "Test Flickr", "With authorization", flickr, new OnBMClickListener() {
			@Override
			public void onBoomButtonClick(int index) {
				EpictureClientAbstract client = new FlickrClient.FlickrClientBuilder()
						.clientSecret("80861547e3099611")
						.clientId("e00ba2ba9f093e1b4d777d24fd3c2d9f")
						.build();
				doAuthorize(client);
			}
		}));
		bmb.addBuilder(createHamButton(Color.DKGRAY, "Test Imgur", "Without authorization", imgur, new OnBMClickListener() {
			@Override
			public void onBoomButtonClick(int index) {
				EpictureClientAbstract client = new ImgurClient.ImgurClientBuilder()
						.clientSecret("e18f03df7f0e0bac37285b83f1b4264644d230d2")
						.clientId("3560cc6fe6a380b")
						.accessToken("d742b4041b4fa59039b9d651587d009e8eaca2e4")
						.refreshToken("56c8ef00bc5185969d62b4f3f222903e8764ed08")
						.build();
				doTests(client);
			}
		}));
		bmb.addBuilder(createHamButton(Color.DKGRAY, "Test Imgur", "With authorization", imgur, new OnBMClickListener() {
			@Override
			public void onBoomButtonClick(int index) {
				EpictureClientAbstract client = new ImgurClient.ImgurClientBuilder()
						.clientSecret("e18f03df7f0e0bac37285b83f1b4264644d230d2")
						.clientId("3560cc6fe6a380b")
						.build();
				doAuthorize(client);
			}
		}));
	}

	private HamButton.Builder createHamButton(int color, String text, String subText, Drawable drawable, OnBMClickListener listener) {
		return new HamButton.Builder()
				.normalImageDrawable(drawable)
				.imagePadding(new Rect(8, 8, 8, 8))
				.normalText(text)
				.subNormalText(subText)
				.containsSubText(true)
				.shadowEffect(true)
				.shadowRadius(Util.dp2px(2))
				.rippleEffect(false)
				.normalColor(color)
				.highlightedColor(ColorUtils.darken(ColorUtils.darken(color)))
				.listener(listener);
	}

	private void doTests(final EpictureClientAbstract client) {
		doMe(client);
		doFavoriteImage(client);
		doGetImage(client);
	}

	private void doAuthorize(final EpictureClientAbstract client) {
		client.authorize(this, new EpictureCallbackInterface<EpictureAuthorization>() {
			@Override
			public void success(EpictureResponseWrapper<EpictureAuthorization> response) {
				mAdapter.add("Access token : " + response.data.getAccessToken());
				mAdapter.add("Refresh token : " + response.data.getRefreshToken());
				Log.d(TAG, "Access token : " + response.data.getAccessToken());
				Log.d(TAG, "Refresh token : " + response.data.getRefreshToken());
				doTests(client);
			}

			@Override
			public void error(EpictureResponseWrapper<EpictureError> error) {
				mAdapter.add(error.data.getPrettyError());
				Log.d(TAG, error.data.getPrettyError());
			}
		});
	}

	private void doFavoriteImage(final EpictureClientAbstract client) {
		client.favoriteImage("Uo6bfo4", new EpictureCallbackInterface<EpicturePicture>() {
			@Override
			public void success(EpictureResponseWrapper<EpicturePicture> response) {
				mAdapter.add("Url : " + response.data.getUrl());
				mAdapter.add("Thumbnail : " + response.data.getThumbnail());
				Log.d(TAG, "Url : " + response.data.getUrl());
				Log.d(TAG, "Thumbnail : " + response.data.getThumbnail());
			}

			@Override
			public void error(EpictureResponseWrapper<EpictureError> error) {
				mAdapter.add(error.data.getPrettyError());
				Log.d(TAG, error.data.getPrettyError());
			}
		});
	}

	private void doGetImage(final EpictureClientAbstract client) {
		client.getImage("Uo6bfo4", new EpictureCallbackInterface<EpicturePicture>() {
			@Override
			public void success(EpictureResponseWrapper<EpicturePicture> response) {
				mAdapter.add("Url : " + response.data.getUrl());
				mAdapter.add("Thumbnail : " + response.data.getThumbnail());
				Log.d(TAG, "Url : " + response.data.getUrl());
				Log.d(TAG, "Thumbnail : " + response.data.getThumbnail());
			}

			@Override
			public void error(EpictureResponseWrapper<EpictureError> error) {
				mAdapter.add(error.data.getPrettyError());
				Log.d(TAG, error.data.getPrettyError());
			}
		});
	}

	private void doMe(final EpictureClientAbstract client) {
		client.me(new EpictureCallbackInterface<EpictureUser>() {
			@Override
			public void success(EpictureResponseWrapper<EpictureUser> response) {
				mAdapter.add("Id : " + response.data.getId());
				Log.d(TAG, "Id : " + response.data.getId());
			}

			@Override
			public void error(EpictureResponseWrapper<EpictureError> error) {
				mAdapter.add(error.data.getPrettyError());
				Log.d(TAG, error.data.getPrettyError());
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