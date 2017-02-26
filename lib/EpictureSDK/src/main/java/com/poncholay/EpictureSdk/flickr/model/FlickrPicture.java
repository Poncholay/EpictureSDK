package com.poncholay.EpictureSdk.flickr.model;

import com.poncholay.EpictureSdk.model.EpicturePicture;
import com.poncholay.EpictureSdk.model.response.EpictureResponseArrayWrapper;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public class FlickrPicture implements EpicturePicture {
	private String id;
	private String title;
	private String url_o;
	private String url_m;

	@Override
	public String getUrl() {
		return getUrl_o();
	}

	@Override
	public String getThumbnail() {
		return getUrl_m();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl_o() {
		return url_o;
	}

	public void setUrl_o(String url_o) {
		this.url_o = url_o;
	}

	public String getUrl_m() {
		return url_m;
	}

	public void setUrl_m(String url_m) {
		this.url_m = url_m;
	}

	public class FlickrPictureWrapperEpicture extends EpictureResponseWrapper<FlickrPicture> {}
	public class FlickrPictureArrayWrapperEpicture extends EpictureResponseArrayWrapper<FlickrPicture> {}
}
