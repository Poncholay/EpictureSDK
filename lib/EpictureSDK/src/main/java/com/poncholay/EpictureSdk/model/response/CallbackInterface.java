package com.poncholay.EpictureSdk.model.response;

import com.poncholay.EpictureSdk.model.EpictureError;

public interface CallbackInterface<T> {
	void success(ResponseWrapper<T> response);
	void error(ResponseWrapper<EpictureError> error);
}

