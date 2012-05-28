/**
 * =====================================================================
 *
 * @file  ReflectedImageview.java
 * @Module Name   com.lenovo.leos.commonwidgets
 * @author yueliang@use.com.cn
 * @OS version  LeOS2.0
 * @Product type: Lepad
 * @date   2010-11-17
 * @brief  This file is the http **** implementation.
 * @This file is responsible by HomePane TEAM.
 * @Comments: 
 * =====================================================================
 *                   Lenovo Confidential Proprietary
 *                    Lenovo Beijing Limited
 *           (c) Copyright Lenovo 2010, All Rights Reserved
 *
 * Revision History:
 *
 *                   Modification  Tracking
 *
 * Author					Date            OS version        Reason 
 * ----------			------------     -------------     -----------
 * yueliang@use.com.cn   2010-11-17         LeOS2.0       Check for NULL, 0 h/w
 * =====================================================================
 **/
package com.adtworker.mail.customwidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.view.View;

public class ReflectedImageview extends View {

	public ReflectedImageview(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

	}

	private Bitmap createReflectedImage(Context context, Bitmap originalImage) {
		// The gap we want between the reflection and the original image
		final int reflectionGap = 4;

		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		// This will not scale but will flip on the Y axis
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		// Create a Bitmap with the flip matrix applied to it.
		// We only want the bottom half of the image
		// Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
		// 0, width, height, matrix, false);
		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
				83 * height / 100, width, 17 * height / 100, matrix, false);

		// Create a new bitmap with same width but taller to fit reflection
		// Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
		// (height + height), Config.ARGB_8888);
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + 17 * height / 100), Config.ARGB_8888);

		// Create a new Canvas with the bitmap that's big enough for
		// the image plus gap plus reflection
		Canvas canvas = new Canvas(bitmapWithReflection);
		// Draw in the original image
		canvas.drawBitmap(originalImage, 0, 0, null);
		// Draw in the gap
		Paint defaultPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
		// Draw in the reflection
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

		// Create a shader that is a linear gradient that covers the reflection
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0,
				originalImage.getHeight(), 0, bitmapWithReflection.getHeight()
						+ reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
		// Set the paint to use this shader (linear gradient)
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);
		reflectionImage.recycle();
		return bitmapWithReflection;
	}

	public void reflect(Bitmap[] myBitmap) {
		// TODO Auto-generated method stub
		for (int i = 0; i < myBitmap.length; i++) {

			Bitmap myBitmapnew = myBitmap[i];
			myBitmap[i] = createReflectedImage(this.getContext(), myBitmapnew);

		}
	}

}
