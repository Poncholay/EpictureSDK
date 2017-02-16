package com.poncholay.EpictureSdk.model.response;

import com.poncholay.EpictureSdk.model.EpictureError;

public interface EpictureCallbackInterface<T> {
	void success(EpictureResponseWrapper<T> response);
	void error(EpictureResponseWrapper<EpictureError> error);
}

