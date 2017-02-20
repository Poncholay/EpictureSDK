package com.poncholay.EpictureSdk.imgur.model;

import com.poncholay.EpictureSdk.model.EpicturePicture;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public class ImgurPicture implements EpicturePicture {
	private long id;
	private String title;
	private String link;

	@Override
	public String getUrl() {
		return getLink();
	}

	@Override
	public String getThumbnail() {
		String[] parts = getUrl().split("\\.");
		if (parts.length < 2) {
			return getUrl();
		}
		String url = "";
		for (int i = 0; i < parts.length; i++) {
			url += i == parts.length - 2 ? (parts[i] + "t") : parts[i];
			url += i == parts.length - 1 ? "" : ".";
		}
		return url;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
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

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public class ImgurPictureWrapperEpicture extends EpictureResponseWrapper<ImgurPicture> {}
}
