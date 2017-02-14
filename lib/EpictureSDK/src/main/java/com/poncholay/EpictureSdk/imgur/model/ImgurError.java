package com.poncholay.EpictureSdk.imgur.model;

public class ImgurError {
	private String error;
	private String request;
	private String method;

	public String getPrettyError() {
		return "Error : (" + method + ") " + request + " : " + error;
	}

	public String getError() {
		return error;
	}

	public String getRequest() {
		return request;
	}

	public String getMethod() {
		return method;
	}

	public class ImgurErrorWrapper extends ImgurResponseWrapper<ImgurError> {}
}