package com.poncholay.EpictureSdk.flickr.model;

import com.poncholay.EpictureSdk.model.EpictureError;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public class FlickrError implements EpictureError {
	private String error;
	private String SDKMethod;

	public FlickrError() {
		this("", "");
	}

	public FlickrError(String error) {
		this(error, "");
	}

	public FlickrError(String error, String SDKMethod) {
		this.error = error;
		this.SDKMethod = SDKMethod;
	}

	@Override
	public String getPrettyError() {
		return "Flickr error : " + error;
	}

	@Override
	public String getError() {
		return error;
	}

	@Override
	public void setError(String error) {
		this.error = error;
	}

	@Override
	public String getSDKMethod() {
		return SDKMethod;
	}

	@Override
	public void setSDKMethod(String SDKMethod) {
		this.SDKMethod = SDKMethod;
	}

	public class FlickrErrorWrapperEpicture extends EpictureResponseWrapper<FlickrError> {}
}
