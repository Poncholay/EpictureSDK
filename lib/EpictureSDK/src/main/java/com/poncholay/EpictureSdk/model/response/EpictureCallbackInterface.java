package com.poncholay.EpictureSdk.model.response;

import com.poncholay.EpictureSdk.model.EpictureError;

public abstract class EpictureCallbackInterface<T> {
	public void success(EpictureResponseWrapper<T> response) {}
	public void success(EpictureResponseArrayWrapper<T> response) {}
	public void error(EpictureResponseWrapper<EpictureError> error) {}
}

