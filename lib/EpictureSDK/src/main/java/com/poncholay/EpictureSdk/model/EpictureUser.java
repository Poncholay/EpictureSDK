package com.poncholay.EpictureSdk.model;

import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public interface EpictureUser {
	public long getId();
	public void setId(long id);
	public class EpictureUserWrapperEpicture extends EpictureResponseWrapper<EpictureUser> {}
}
