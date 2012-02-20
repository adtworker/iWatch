package com.norman.apps;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LiveWallpaper extends WallpaperService
		implements
			OnSharedPreferenceChangeListener {

	private final String TAG = "LiveWallpaper";
	private final Handler handler = new Handler();
	private SharedPreferences mSharedPref;
	private String strPicCode;
	private Bitmap bm = null;

	@Override
	public void onCreate() {
		System.out.println("service onCreate");
		super.onCreate();
		mSharedPref = getSharedPreferences("iWatch", Context.MODE_PRIVATE);
		mSharedPref.registerOnSharedPreferenceChangeListener(this);
		makePrefChanges();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		makePrefChanges();
	}

	private String getPicPath() {
		return Environment.getDataDirectory() + WatchActivity.APP_FOLDER
				+ WatchActivity.PIC_FOLDER + File.separator + strPicCode;
	}

	private void makePrefChanges() {
		strPicCode = mSharedPref.getString("pic_code", "");
		Log.d(TAG, "strPicCode is " + strPicCode);

		if (strPicCode == "") {
			bm = BitmapFactory.decodeResource(getResources(),
					R.drawable.clock_dial);

		} else {
			try {

				DisplayMetrics displayMetrics = getResources()
						.getDisplayMetrics();
				int width = displayMetrics.widthPixels;
				int height = displayMetrics.heightPixels;

				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inJustDecodeBounds = true;
				// bm = BitmapFactory.decodeFile(getPicPath(), opt);
				bm = BitmapFactory.decodeStream(getAssets().open(strPicCode),
						null, opt);

				int bm_w = opt.outWidth;
				int bm_h = opt.outHeight;
				Log.d(TAG, "origin: " + bm_w + "x" + bm_h);
				if (bm_w > width || bm_h > height) {
					float ratio_hw = (float) bm_h / bm_w;
					Log.d(TAG, "bitmap original ratio height/width = "
							+ ratio_hw);
					if (height / ratio_hw <= width) {
						opt.outHeight = height;
						opt.outWidth = (int) (height / ratio_hw);
					} else {
						opt.outHeight = (int) (width * ratio_hw);
						opt.outWidth = width;
					}
					Log.d(TAG, "scaled: " + opt.outWidth + "x" + opt.outHeight);
				}

				opt.inJustDecodeBounds = false;
				opt.inSampleSize = bm_w / width;
				Log.d(TAG, "bitmap inSampleSize = " + opt.inSampleSize);

				// bm = BitmapFactory.decodeFile(getPicPath(), opt);
				bm = BitmapFactory.decodeStream(getAssets().open(strPicCode),
						null, opt);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	@Override
	public void onDestroy() {
		System.out.println("service onDestory");
		super.onDestroy();
		mSharedPref.unregisterOnSharedPreferenceChangeListener(this);

		if (bm != null)
			bm.recycle();
	}

	@Override
	public Engine onCreateEngine() {
		System.out.println("service onCreateEngine");
		return new MyEngine();
	}

	private class MyEngine extends Engine {

		private final Paint mPaint;
		private final Paint mPaintLine;

		private final Runnable drawThread = new Runnable() {
			@Override
			public void run() {
				drawWallpaper();
			}
		};

		public MyEngine() {
			System.out.println("MyEngine");
			mPaint = new Paint();
			mPaint.setColor(Color.WHITE);
			mPaint.setAntiAlias(true);
			mPaint.setStrokeWidth(2);
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setTextSize(60);

			mPaintLine = new Paint();
			mPaintLine.setColor(Color.WHITE);
			mPaintLine.setAntiAlias(true);
			mPaintLine.setStrokeWidth(4);
			mPaintLine.setStyle(Paint.Style.STROKE);
		}

		@Override
		public Bundle onCommand(String action, int x, int y, int z,
				Bundle extras, boolean resultRequested) {
			// System.out.println("onCommand");
			return super.onCommand(action, x, y, z, extras, resultRequested);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			// System.out.println("onCreate");
			super.onCreate(surfaceHolder);
			// 作用是使壁纸能响应touch event，默认是false
			setTouchEventsEnabled(true);
		}

		@Override
		public void onDestroy() {
			// System.out.println("onDestroy");
			super.onDestroy();
			handler.removeCallbacks(drawThread);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			System.out.println("onoffsetsChanged");
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			// System.out.println("onVisibilityChanged");
			if (visible) {
				// 开始
				handler.postDelayed(drawThread, 100);
			} else {
				handler.removeCallbacks(drawThread);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			// System.out.println("onSurfaceChanged");
			super.onSurfaceChanged(holder, format, width, height);
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			// System.out.println("onSurfaceCreated");
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			// System.out.println("onSurfaceDestoryed");
			super.onSurfaceDestroyed(holder);
			handler.removeCallbacks(drawThread);
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			// System.out.println("onTouchEvent");
			super.onTouchEvent(event);

		}

		private void drawWallpaper() {

			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = holder.lockCanvas();
			drawTime(canvas);
			holder.unlockCanvasAndPost(canvas);

			handler.postDelayed(drawThread, 100);
		}

		private void drawTime(Canvas canvas) {
			long curTimeMillis = System.currentTimeMillis();
			Date date = new Date(curTimeMillis);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dataStr = sdf.format(date);
			canvas.save();
			canvas.drawColor(0xff000000);
			mPaint.setTextSize(60);
			// canvas.drawText(dataStr, x, y, mPaint);

			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
			int mWidth = displayMetrics.widthPixels;
			int mHeight = displayMetrics.heightPixels;

			float r = Math.min(mWidth, mHeight) / 3;
			float x = (mWidth - r * 2) / 2;
			float y = (mHeight - r * 2) / 2;
			float x1 = x + r;
			float y1 = y + r;
			float x2 = 0;
			float y2 = 0;

			if (bm != null) {

				int bm_w = bm.getWidth();
				int bm_h = bm.getHeight();
				r = (Math.min(bm_w, bm_h)) / 2;
				x = (mWidth - bm_w) / 2;
				y = (mHeight - bm_h) / 2;
				x1 = x + bm_w / 2;
				y1 = y + bm_h / 2;
				canvas.drawBitmap(bm, x, y, mPaint);
			}

			float r1 = r; // radius in total
			float r0 = 6; // center point
			double rad = 0;

			float rSec = r1 * 4 / 5;
			float rMin = r1 * 2 / 3;
			float rHour = r1 / 2;

			float nSec = 2;
			float nMin = 6;
			float nHour = 8;

			float rMk1 = 10;
			float rMk2 = 4;

			if (bm == null) {
				// Drawing the panel
				mPaintLine.setStrokeWidth(4);
				canvas.drawCircle(x1, y1, r1, mPaintLine);

				// drawing hour mark
				for (int i = 0, total = 12; i < total; i++) {
					float x3, y3;
					rad = Math.toRadians(180 - (i % total) * 360 / total);
					x2 = x1 + (float) (r1 * Math.sin(rad));
					y2 = y1 + (float) (r1 * Math.cos(rad));
					x3 = x1 + (float) ((r1 - rMk1) * Math.sin(rad));
					y3 = y1 + (float) ((r1 - rMk1) * Math.cos(rad));
					canvas.drawLine(x2, y2, x3, y3, mPaintLine);
				}
				// drawing minute mark
				for (int i = 0, total = 60; i < total; i++) {
					float x3, y3;
					rad = Math.toRadians(180 - (i % total) * 360 / total);
					x2 = x1 + (float) (r1 * Math.sin(rad));
					y2 = y1 + (float) (r1 * Math.cos(rad));
					x3 = x1 + (float) ((r1 - rMk2) * Math.sin(rad));
					y3 = y1 + (float) ((r1 - rMk2) * Math.cos(rad));
					canvas.drawLine(x2, y2, x3, y3, mPaintLine);
				}

				// Drawing time string
				sdf.applyPattern("HH:mm:ss");
				dataStr = sdf.format(date);
				mPaint.setTextSize(30);
				canvas.drawText(dataStr, x1 - 60, y1 + 60, mPaint);

				// Drawing date string
				sdf.applyPattern("dd");
				dataStr = sdf.format(date);
				canvas.drawText(dataStr, x1 + r1 / 3, y1 + 10, mPaint);
				// mPaintLine.setStrokeWidth(2);
				// canvas.drawRect(x1+115, y1-20, x1+158, y1+20, mPaintLine);
			}

			// Drawing hour
			drawClockHand(canvas, mPaintLine, x1, y1, date.getHours() % 12 * 5
					+ date.getMinutes() / 12, 60, rHour, nHour, Color.DKGRAY);

			// Drawing minute
			drawClockHand(canvas, mPaintLine, x1, y1, date.getMinutes(), 60,
					rMin, nMin, Color.DKGRAY);

			// Drawing second
			drawClockHand(canvas, mPaintLine, x1, y1, date.getSeconds(), 60,
					rSec, nSec, Color.RED);

			int iColor = mPaint.getColor();
			mPaint.setColor(Color.WHITE);
			canvas.drawCircle(x1, y1, r0, mPaint);
			mPaint.setColor(iColor);

			canvas.restore();
		}
	}

	private void drawClockHand(Canvas canvas, Paint paint, float x, float y,
			int current, int total, float length, float width, int color) {
		double radian;
		float x1, y1, x2, y2;
		float lentail = length / 4;
		int iColor = paint.getColor();
		paint.setColor(color);
		float iWidth = paint.getStrokeWidth();
		paint.setStrokeWidth(width);

		radian = Math.toRadians(180 - 360 * current / total);
		x1 = x + (float) (length * Math.sin(radian));
		y1 = y + (float) (length * Math.cos(radian));

		radian = Math.toRadians(360 - 360 * current / total);
		x2 = x + (float) (lentail * Math.sin(radian));
		y2 = y + (float) (lentail * Math.cos(radian));

		canvas.drawLine(x1, y1, x2, y2, paint);

		paint.setStrokeWidth(iWidth);
		paint.setColor(iColor);
	}
}
