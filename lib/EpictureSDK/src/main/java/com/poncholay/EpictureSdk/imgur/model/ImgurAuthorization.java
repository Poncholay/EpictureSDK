package com.poncholay.EpictureSdk.imgur.model;

import com.poncholay.EpictureSdk.model.EpictureAuthorization;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public class ImgurAuthorization implements EpictureAuthorization {
	private String accessToken;
	private String refreshToken;

	@Override
	public String getAccessToken() {
		return accessToken;
	}

	@Override
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public String getRefreshToken() {
		return refreshToken;
	}

	@Override
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public class ImgurAuthorizationWrapperEpicture extends EpictureResponseWrapper<ImgurAuthorization> {}
}