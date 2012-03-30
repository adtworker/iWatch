package com.adtworker.mail.service.data;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class UtilImgTrans {

	public static byte[] Bitmap2Byte(Bitmap bm) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static Bitmap Byte2Bimap(byte[] b) {
		if (b.length == 0) {
			return null;
		}
		return BitmapFactory.decodeByteArray(b, 0, b.length);
	}

}
