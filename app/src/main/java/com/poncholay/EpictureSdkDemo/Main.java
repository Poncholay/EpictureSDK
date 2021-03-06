package com.poncholay.EpictureSdkDemo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ListView;

import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.nightonke.boommenu.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.poncholay.EpictureSdk.EpictureClientAbstract;
import com.poncholay.EpictureSdk.flickr.FlickrClient;
import com.poncholay.EpictureSdk.imgur.ImgurClient;
import com.poncholay.EpictureSdk.model.EpictureAuthorization;
import com.poncholay.EpictureSdk.model.EpictureError;
import com.poncholay.EpictureSdk.model.EpicturePicture;
import com.poncholay.EpictureSdk.model.response.EpictureCallbackInterface;
import com.poncholay.EpictureSdk.model.response.EpictureResponseArrayWrapper;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

import java.util.ArrayList;
import java.util.List;

public class Main extends Activity {

	private static final String TAG = "Main";

	private ResponseAdapter mAdapter;

	@Override
	protected void onCreate(Bundle ignored) {
		super.onCreate(ignored);

		setContentView(R.layout.sample_view);

		ListView mListView = (ListView) findViewById(R.id.list_view);
		mAdapter = new ResponseAdapter(this, new ArrayList<String>());
		mListView.setAdapter(mAdapter);

		createBoomMenu();
		if (!ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
		}
	}

	private void createBoomMenu() {
		BoomMenuButton bmb = (BoomMenuButton) findViewById(R.id.bmb);
		bmb.setButtonEnum(ButtonEnum.Ham);
		bmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_5);
		bmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_5);

		Drawable clear = ContextCompat.getDrawable(this, R.drawable.clear);
		Drawable imgur = ContextCompat.getDrawable(this, R.drawable.imgur);
		Drawable flickr = ContextCompat.getDrawable(this, R.drawable.flickr);

		final Activity activity = this;

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
				EpictureClientAbstract client = new FlickrClient.FlickrClientBuilder(activity)
						.clientSecret("80861547e3099611")
						.clientId("e00ba2ba9f093e1b4d777d24fd3c2d9f")
						.build();
				doTests(client);
			}
		}));
		bmb.addBuilder(createHamButton(ColorUtils.darken(Color.RED), "Test Flickr", "With authorization", flickr, new OnBMClickListener() {
			@Override
			public void onBoomButtonClick(int index) {
				EpictureClientAbstract client = new FlickrClient.FlickrClientBuilder(activity)
						.clientSecret("80861547e3099611")
						.clientId("e00ba2ba9f093e1b4d777d24fd3c2d9f")
						.build();
				doAuthorize(client);
			}
		}));
		bmb.addBuilder(createHamButton(Color.DKGRAY, "Test Imgur", "Without authorization", imgur, new OnBMClickListener() {
			@Override
			public void onBoomButtonClick(int index) {
				EpictureClientAbstract client = new ImgurClient.ImgurClientBuilder(activity)
						.clientSecret("e18f03df7f0e0bac37285b83f1b4264644d230d2")
						.clientId("3560cc6fe6a380b")
						.build();
				doTests(client);
			}
		}));
		bmb.addBuilder(createHamButton(Color.DKGRAY, "Test Imgur", "With authorization", imgur, new OnBMClickListener() {
			@Override
			public void onBoomButtonClick(int index) {
				EpictureClientAbstract client = new ImgurClient.ImgurClientBuilder(activity)
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
		mAdapter.add("Access token : " + client.getAccessToken());
		mAdapter.add("Refresh token : " + client.getRefreshToken());
		mAdapter.add("ClientId : " + client.getClientId());
		mAdapter.add("ClientSecret : " + client.getClientSecret());
		mAdapter.add("Username : " + client.getUsername());
		doFavoriteImage(client);
		doGetImage(client);
		doGetImages(client);
		doGetFavorites(client);
		doSearchImages(client);
		doUploadImage(client);
	}

	private void printError(EpictureResponseWrapper<EpictureError> error) {
		mAdapter.add(error.data.getPrettyError());
		Log.d(TAG, error.data.getPrettyError());
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
				printError(error);
			}
		});
	}

	private void doFavoriteImage(final EpictureClientAbstract client) {
		EpictureCallbackInterface<Boolean> callback = new EpictureCallbackInterface<Boolean>() {
			@Override
			public void success(EpictureResponseWrapper<Boolean> response) {
				mAdapter.add("Favorited : " + response.data);
				Log.d(TAG, "Favorited : " + response.data);
			}

			@Override
			public void error(EpictureResponseWrapper<EpictureError> error) {
				printError(error);
			}
		};
		client.favoriteImage("Uo6bfo4", callback);
		client.favoriteImage(null, callback);
		client.favoriteImage("27152461283", callback);
		client.unfavoriteImage("Uo6bfo4", callback);
		client.unfavoriteImage(null, callback);
		client.unfavoriteImage("27152461283", callback);
	}

	private void doGetImage(final EpictureClientAbstract client) {
		EpictureCallbackInterface<EpicturePicture> callback = new EpictureCallbackInterface<EpicturePicture>() {
			@Override
			public void success(EpictureResponseWrapper<EpicturePicture> response) {
				mAdapter.add("Url : " + response.data.getUrl());
				mAdapter.add("Thumbnail : " + response.data.getThumbnail());
				mAdapter.add("Id : " + response.data.getId());
				Log.d(TAG, "Url : " + response.data.getUrl());
				Log.d(TAG, "Thumbnail : " + response.data.getThumbnail());
				Log.d(TAG, "Id : " + response.data.getId());
			}

			@Override
			public void error(EpictureResponseWrapper<EpictureError> error) {
				printError(error);
			}
		};
		client.getImage("Uo6bfo4", callback);
		client.getImage("32742182460", callback);
		client.getImage(null, callback);
		client.getImage("nopeFail", callback);
	}

	private void doGetImages(final EpictureClientAbstract client) {
		EpictureCallbackInterface<EpicturePicture> callback = new EpictureCallbackInterface<EpicturePicture>() {
			@Override
			public void success(EpictureResponseArrayWrapper<EpicturePicture> response) {
				List<EpicturePicture> pictures = response.data;

				if (pictures.size() == 0) {
					mAdapter.add("No results");
					Log.d(TAG, "No results");
				}
				for (EpicturePicture picture : pictures) {
					mAdapter.add("Url : " + picture.getUrl());
					mAdapter.add("Thumbnail : " + picture.getThumbnail());
					mAdapter.add("Id : " + picture.getId());
					Log.d(TAG, "Url : " + picture.getUrl());
					Log.d(TAG, "Thumbnail : " + picture.getThumbnail());
					Log.d(TAG, "Id : " + picture.getId());
				}
			}

			@Override
			public void error(EpictureResponseWrapper<EpictureError> error) {
				printError(error);
			}
		};
		client.getImages(callback);
		client.getImages(0, callback);
		client.getImages(1, callback);
		client.getImages("JinLaPiscine", callback);
		client.getImages(null, callback);
		client.getImages("EpictureJin", 0, callback);
		client.getImages("EpictureJinpron", 1, callback);
	}

	private void doGetFavorites(final EpictureClientAbstract client) {
		EpictureCallbackInterface<EpicturePicture> callback = new EpictureCallbackInterface<EpicturePicture>() {
			@Override
			public void success(EpictureResponseArrayWrapper<EpicturePicture> response) {
				List<EpicturePicture> pictures = response.data;

				if (pictures.size() == 0) {
					mAdapter.add("No results");
					Log.d(TAG, "No results");
				}
				for (EpicturePicture picture : pictures) {
					mAdapter.add("Url : " + picture.getUrl());
					mAdapter.add("Thumbnail : " + picture.getThumbnail());
					mAdapter.add("Id : " + picture.getId());
					Log.d(TAG, "Url : " + picture.getUrl());
					Log.d(TAG, "Thumbnail : " + picture.getThumbnail());
					Log.d(TAG, "Id : " + picture.getId());
				}
			}

			@Override
			public void error(EpictureResponseWrapper<EpictureError> error) {
				printError(error);
			}
		};
		client.getFavoriteImages(callback);
		client.getFavoriteImages(0, callback);
	}

	private void doSearchImages(final EpictureClientAbstract client) {
		EpictureCallbackInterface<EpicturePicture> callback = new EpictureCallbackInterface<EpicturePicture>() {
			@Override
			public void success(EpictureResponseArrayWrapper<EpicturePicture> response) {
				List<EpicturePicture> pictures = response.data;

				if (pictures.size() == 0) {
					mAdapter.add("No results");
					Log.d(TAG, "No results");
				}
				for (EpicturePicture picture : pictures) {
					mAdapter.add("Url : " + picture.getUrl());
					mAdapter.add("Thumbnail : " + picture.getThumbnail());
					mAdapter.add("Id : " + picture.getId());
					Log.d(TAG, "Url : " + picture.getUrl());
					Log.d(TAG, "Thumbnail : " + picture.getThumbnail());
					Log.d(TAG, "Id : " + picture.getId());
				}
			}

			@Override
			public void error(EpictureResponseWrapper<EpictureError> error) {
				printError(error);
			}
		};
		client.searchImages("MilcenSlip", callback);
		client.searchImages("Nyan cat", 0, callback);
		client.searchImages("", 2, callback);
		client.searchMyImages("MilcenSlip", callback);
		client.searchMyImages("suuu", 0, callback);
		client.searchMyImages("", 2, callback);
	}

	private void doUploadImage(final EpictureClientAbstract client) {
		EpictureCallbackInterface<EpicturePicture> callback = new EpictureCallbackInterface<EpicturePicture>() {
			@Override
			public void success(EpictureResponseWrapper<EpicturePicture> response) {
				mAdapter.add("Url : " + response.data.getUrl());
				mAdapter.add("Thumbnail : " + response.data.getThumbnail());
				Log.d(TAG, "Url : " + response.data.getUrl());
				Log.d(TAG, "Thumbnail : " + response.data.getThumbnail());
			}

			@Override
			public void error(EpictureResponseWrapper<EpictureError> error) {
				printError(error);
			}
		};
//		client.uploadImage("nopeFail", callback);
		client.uploadImage("/storage/sdcard1/DCIM/100ANDRO/DSC_0483.JPG", callback);
//		client.uploadImage("/storage/sdcard1/DCIM/100ANDRO/DSC_0483.JPG", null, "Test", "Test api", "Test api", callback);
	}
}

//	Flickr and Imgur API implementation
//		OK 		• Connecting to the Flickr and Imgur platforms
//		OK 		• The photo display put online by the user connected to Flickr and Imgur
//		OK 		• Flickr and Imgur photo finder
//		IM 		• Uploading photos to Flickr and Imgur
//		OK 		• Adding/deleting photos to/from your favorites
//		.. 		• Managing photo display filters
