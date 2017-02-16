package com.poncholay.EpictureSdk.model.response;

public class EpictureResponseWrapper<T> {
	public T data;
	public boolean success;
	public int status;

	public EpictureResponseWrapper() {
		this(false, 0, null);
	}

	public EpictureResponseWrapper(boolean success, int status, T data) {
		this.success = success;
		this.status = status;
		this.data = data;
	}
}
