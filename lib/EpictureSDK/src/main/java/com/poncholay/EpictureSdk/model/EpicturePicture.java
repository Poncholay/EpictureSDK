package com.poncholay.EpictureSdk.model;

import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public interface EpicturePicture {
	String getUrl();
	String getThumbnail();

	String getId();
	void setId(String id);
	String getTitle();
	void setTitle(String title);

	class EpictureUserWrapperEpicture extends EpictureResponseWrapper<EpictureUser> {}
}
