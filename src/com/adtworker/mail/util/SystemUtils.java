package com.adtworker.mail.util;

import android.util.Log;
import android.view.Display;

import com.adtworker.mail.constants.Constants;

public class SystemUtils {

	public static Integer[] getDipslayWidthAndHeight(Display display) {

		Integer[] result = new Integer[]{display.getWidth(),
				display.getHeight()};
		Log.w(Constants.TAG, "width:" + result[0] + ",height:" + result[1]);
		return result;
	}
}
