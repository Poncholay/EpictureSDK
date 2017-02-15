package com.poncholay.EpictureSdk.model;

import com.poncholay.EpictureSdk.model.response.ResponseWrapper;

public interface EpictureUser {
	public long getId();
	public void setId(long id);
	public class EpictureUserWrapper extends ResponseWrapper<EpictureUser> {}
}
