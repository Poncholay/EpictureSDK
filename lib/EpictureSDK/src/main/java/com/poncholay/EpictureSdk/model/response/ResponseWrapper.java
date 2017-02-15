package com.poncholay.EpictureSdk.model.response;

public class ResponseWrapper<T> {
	public T data;
	public boolean success;
	public int status;

	public ResponseWrapper() {
		this(false, 0, null);
	}

	public ResponseWrapper(boolean success, int status, T data) {
		this.success = success;
		this.status = status;
		this.data = data;
	}
}
