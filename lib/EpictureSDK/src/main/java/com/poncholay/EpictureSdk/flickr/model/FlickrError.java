package com.poncholay.EpictureSdk.flickr.model;

import com.poncholay.EpictureSdk.model.EpictureError;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public class FlickrError implements EpictureError {
	private String error;

	@Override
	public String getPrettyError() {
		return "Error : " + error;
	}

	@Override
	public String getError() {
		return error;
	}

	@Override
	public void setError(String error) {
		this.error = error;
	}

	public class FlickrErrorWrapperEpicture extends EpictureResponseWrapper<FlickrError> {}
}
