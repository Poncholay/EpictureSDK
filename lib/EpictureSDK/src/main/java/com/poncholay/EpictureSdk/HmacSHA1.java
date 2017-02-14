package com.poncholay.EpictureSdk;

import android.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacSHA1 {
	public static String hmacSha1(String value, String key) {
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec secret = new SecretKeySpec(key.getBytes("UTF-8"), mac.getAlgorithm());
			mac.init(secret);

			byte[] digest = mac.doFinal(value.getBytes());

			return Base64.encodeToString(digest, 0);
		} catch (Exception e) {
			return value;
		}
	}
}
