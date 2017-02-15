package com.poncholay.EpictureSdk.imgur.model;

import com.poncholay.EpictureSdk.model.EpictureError;
import com.poncholay.EpictureSdk.model.response.ResponseWrapper;

public class ImgurError implements EpictureError {
	private String error;
	private String request;
	private String method;

	@Override
	public String getPrettyError() {
		return "Error : (" + method + ") " + request + " : " + error;
	}

	@Override
	public String getError() {
		return error;
	}

	@Override
	public void setError(String error) {
		this.error = error;
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

	public class ImgurErrorWrapper extends ResponseWrapper<ImgurError> {}
}