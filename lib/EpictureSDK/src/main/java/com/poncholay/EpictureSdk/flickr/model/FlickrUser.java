package com.poncholay.EpictureSdk.flickr.model;

import com.poncholay.EpictureSdk.model.EpictureUser;
import com.poncholay.EpictureSdk.model.response.ResponseWrapper;

public class FlickrUser implements EpictureUser {
	private long id;

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public class FlickrUserWrapper extends ResponseWrapper<FlickrUser> {}
}
