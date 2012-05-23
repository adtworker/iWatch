package com.adtworker.mail.util;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.adtworker.mail.WatchApp;
import com.adtworker.mail.constants.Constants;

public class SystemUtils {

	/**
	 * 获取屏幕宽高
	 * 
	 * @param display
	 * @return 【宽，高】数组
	 */
	public static Integer[] getDipslayWidthAndHeight(Display display) {

		Integer[] result = new Integer[]{display.getWidth(),
				display.getHeight()};
		Log.w(Constants.TAG, "width:" + result[0] + ",height:" + result[1]);
		return result;
	}

	/**
	 * 获取屏幕宽高
	 * 
	 * @return 【宽，高】数组
	 */
	public static Integer[] getDisplayMetrics() {
		DisplayMetrics displayMetrics = WatchApp.getInstance().getResources()
				.getDisplayMetrics();
		Integer[] result = new Integer[]{displayMetrics.widthPixels,
				displayMetrics.heightPixels};
		Log.v(Constants.TAG, "width:" + result[0] + ",height:" + result[1]);
		return result;
	}
}
