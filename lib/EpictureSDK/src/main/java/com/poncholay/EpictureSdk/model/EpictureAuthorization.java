package com.poncholay.EpictureSdk.model;

import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public interface EpictureAuthorization {
	public String getAccessToken();
	public void setAccessToken(String accessToken);
	public String getRefreshToken();
	public void setRefreshToken(String refreshToken);

	public class EpictureAuthorizationWrapperEpicture extends EpictureResponseWrapper<EpictureAuthorization> {}
}