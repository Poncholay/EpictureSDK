package com.poncholay.EpictureSdk.imgur.model;

import com.poncholay.EpictureSdk.model.EpicturePicture;
import com.poncholay.EpictureSdk.model.response.EpictureResponseArrayWrapper;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public class ImgurPicture implements EpicturePicture {
	private String id;
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
		StringBuilder url = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			url.append(i == parts.length - 2 ? (parts[i] + "t") : parts[i]);
			url.append(i + 1 == parts.length ? "" : ".");
		}
		return url.toString();
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

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public class ImgurPictureWrapperEpicture extends EpictureResponseWrapper<ImgurPicture> {}
	public class ImgurPictureArrayWrapperEpicture extends EpictureResponseArrayWrapper<ImgurPicture> {}
}
