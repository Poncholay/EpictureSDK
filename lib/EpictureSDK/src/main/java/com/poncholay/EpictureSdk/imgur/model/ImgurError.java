package com.poncholay.EpictureSdk.imgur.model;

import com.poncholay.EpictureSdk.model.EpictureError;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public class ImgurError implements EpictureError {
	private String error;
	private String request;
	private String method;
	private String SDKMethod;

	public ImgurError() {
		this("", "");
	}

	public ImgurError(String error) {
		this(error, "");
	}

	public ImgurError(String error, String SDKMethod) {
		this.error = error;
		this.SDKMethod = SDKMethod;
		this.request = "";
		this.method = "";
	}

	@Override
	public String getPrettyError() {
		return "Imgur error : (" + method + ") " + request + " : " + error;
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

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public class ImgurErrorWrapperEpicture extends EpictureResponseWrapper<ImgurError> {}
}