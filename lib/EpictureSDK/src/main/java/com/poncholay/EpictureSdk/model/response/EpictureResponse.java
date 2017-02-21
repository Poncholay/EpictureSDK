package com.poncholay.EpictureSdk.model.response;

public class EpictureResponse {
	public boolean success;
	public int status;

	public EpictureResponse() {}

	public EpictureResponse(boolean success, int status) {
		this.success = success;
		this.status = status;
	}
}
