package com.poncholay.EpictureSdk.model;

import com.poncholay.EpictureSdk.model.response.ResponseWrapper;

public interface EpictureError {
	public String getPrettyError();
	public String getError();
	public void setError(String error);

	public class ErrorWrapper extends ResponseWrapper<Error> {}
}