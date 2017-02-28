package com.poncholay.EpictureSdk.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cz.msebera.android.httpclient.extras.Base64;

public class HmacSHA1 {
	public static String hmacSha1(String value, String key) {
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec secret = new SecretKeySpec(key.getBytes("UTF-8"), mac.getAlgorithm());
			mac.init(secret);

			byte[] digest = mac.doFinal(value.getBytes());

			return Base64.encodeToString(digest, Base64.DEFAULT);
		} catch (Exception e) {
			return value;
		}
	}
}
