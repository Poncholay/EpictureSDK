package com.poncholay.EpictureSdkDemo;

import android.graphics.Color;

public class ColorUtils {
	public static int darken(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.8f;
		return Color.HSVToColor(hsv);
	}
}