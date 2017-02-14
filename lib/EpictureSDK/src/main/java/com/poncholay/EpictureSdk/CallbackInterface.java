package com.poncholay.EpictureSdk;

import com.poncholay.EpictureSdk.imgur.model.ImgurError;
import com.poncholay.EpictureSdk.imgur.model.ImgurResponseWrapper;

public interface CallbackInterface<T> {
	void success(ImgurResponseWrapper<T> response);
	void error(ImgurResponseWrapper<ImgurError> error);
}

