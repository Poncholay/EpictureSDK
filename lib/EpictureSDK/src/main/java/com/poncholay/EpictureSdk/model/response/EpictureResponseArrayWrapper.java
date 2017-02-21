package com.poncholay.EpictureSdk.model.response;

import java.util.List;

public class EpictureResponseArrayWrapper<T> extends EpictureResponse {
	public List<T> data;

	public EpictureResponseArrayWrapper() {
		this(false, 0, null);
	}

	public EpictureResponseArrayWrapper(boolean success, int status, List<T> data) {
		super(success, status);
		this.data = data;
	}
}
