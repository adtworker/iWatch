package com.norman.apps;

import java.lang.reflect.Field;

import android.util.Log;

/*
 * 图片工具类
 * res建立文件夹raw放入工程图片
 */
public class ImageUtil {

	// 获取图片
	public static int getImage(String pic) {
		try {
			Class cl = R.raw.class;
			Field field = cl.getDeclaredField(pic);
			return field.getInt(pic);
		} catch (Exception e) {
			Log.e("ImageUtil", "error", e);
		}
		// 错误时返回默认图片
		return R.raw.m1;
	}
}
