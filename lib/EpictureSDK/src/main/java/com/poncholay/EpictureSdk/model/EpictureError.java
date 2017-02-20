package com.poncholay.EpictureSdk.model;

import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public interface EpictureError {
	public String getPrettyError();

	public String getError();
	public void setError(String error);
	public String getSDKMethod();
	public void setSDKMethod(String SDKMethod);

	public class ErrorWrapperEpicture extends EpictureResponseWrapper<Error> {}
}