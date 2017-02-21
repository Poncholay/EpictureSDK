package com.poncholay.EpictureSdk.model.response;

public class EpictureResponseWrapper<T> extends EpictureResponse {
	public T data;

	public EpictureResponseWrapper() {
		this(false, 0, null);
	}

	public EpictureResponseWrapper(boolean success, int status, T data) {
		super(success, status);
		this.data = data;
	}
}
