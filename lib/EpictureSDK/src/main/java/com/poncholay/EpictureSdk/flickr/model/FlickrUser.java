package com.poncholay.EpictureSdk.flickr.model;

import com.poncholay.EpictureSdk.model.EpictureUser;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public class FlickrUser implements EpictureUser {
	private long id;
	private String username;

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	public class FlickrUserWrapperEpicture extends EpictureResponseWrapper<FlickrUser> {}
}
