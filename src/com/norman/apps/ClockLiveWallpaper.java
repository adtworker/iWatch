package com.norman.apps;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class ClockLiveWallpaper extends WallpaperService {

	private final Handler handler = new Handler();

	@Override
	public void onCreate() {
		System.out.println("service onCreate");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		System.out.println("service onDestory");
		super.onDestroy();
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
			// TODO Auto-generated method stub
			System.out.println("onCommand");
			return super.onCommand(action, x, y, z, extras, resultRequested);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			System.out.println("onCreate");
			super.onCreate(surfaceHolder);
			// 作用是使壁纸能响应touch event，默认是false
			setTouchEventsEnabled(true);
		}

		@Override
		public void onDestroy() {
			System.out.println("onDestroy");
			super.onDestroy();
			handler.removeCallbacks(drawThread);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			// TODO Auto-generated method stub
			System.out.println("onoffsetsChanged");
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			System.out.println("onVisibilityChanged");
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
			System.out.println("onSurfaceChanged");
			// TODO Auto-generated method stub
			super.onSurfaceChanged(holder, format, width, height);
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			System.out.println("onSurfaceCreated");
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			System.out.println("onSurfaceDestoryed");
			// TODO Auto-generated method stub
			super.onSurfaceDestroyed(holder);
			handler.removeCallbacks(drawThread);
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			System.out.println("onTouchEvent");
			// TODO Auto-generated method stub
			super.onTouchEvent(event);

		}

		private void drawWallpaper() {

			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = holder.lockCanvas();
			drawTime(canvas);
			holder.unlockCanvasAndPost(canvas);

			// 循环执行
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

			Bitmap bm = BitmapFactory.decodeResource(getResources(),
					R.drawable.clockdroids_dial);
			if (bm != null) {
				r = (Math.min(bm.getWidth(), bm.getHeight())) / 2;
				mPaint.setColor(Color.GRAY);
				mPaintLine.setColor(Color.GRAY);
			}

			float x = (mWidth - r * 2) / 2;
			float y = (mHeight - r * 2) / 2;

			canvas.drawBitmap(bm, x, y, mPaint);

			float x1 = x + r;
			float y1 = y + r;
			float x2 = 0;
			float y2 = 0;
			float r1 = r - 10; // radius in total
			float r0 = 5; // centre point
			double rad = 0;

			float rSec = r1 - 10;
			float rMin = r1 - 60;
			float rHour = r1 - 99;
			float rSec2 = 40;
			float rMk1 = 10;
			float rMk2 = 4;

			if (bm == null) {
				// Drawing the panel
				mPaintLine.setStrokeWidth(4);
				canvas.drawCircle(x1, y1, r1, mPaintLine);
				canvas.drawCircle(x1, y1, r0, mPaint);

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
			}

			// Drawing second
			rad = Math.toRadians(180 - date.getSeconds() * 360 / 60);
			x2 = x1 + (float) (rSec * Math.sin(rad));
			y2 = y1 + (float) (rSec * Math.cos(rad));
			mPaintLine.setStrokeWidth(2);
			int iColor = mPaintLine.getColor();
			mPaintLine.setColor(Color.RED);
			canvas.drawLine(x1, y1, x2, y2, mPaintLine);

			// Second's tail
			rad = Math.toRadians(360 - date.getSeconds() * 360 / 60);
			x2 = x1 + (float) (rSec2 * Math.sin(rad));
			y2 = y1 + (float) (rSec2 * Math.cos(rad));
			canvas.drawLine(x1, y1, x2, y2, mPaintLine);
			mPaintLine.setColor(iColor);

			// Drawing minute
			rad = Math.toRadians(180 - date.getMinutes() * 360 / 60);
			x2 = x1 + (float) (rMin * Math.sin(rad));
			y2 = y1 + (float) (rMin * Math.cos(rad));
			mPaintLine.setStrokeWidth(4);
			canvas.drawLine(x1, y1, x2, y2, mPaintLine);

			// Drawing hour
			rad = Math.toRadians(180 - (date.getHours() % 12) * 360 / 12);
			x2 = x1 + (float) (rHour * Math.sin(rad));
			y2 = y1 + (float) (rHour * Math.cos(rad));
			mPaintLine.setStrokeWidth(6);
			canvas.drawLine(x1, y1, x2, y2, mPaintLine);

			if (bm == null) {
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

			canvas.restore();
		}
	}

}
