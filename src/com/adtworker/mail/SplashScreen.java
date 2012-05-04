package com.adtworker.mail;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;

public class SplashScreen extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = new SplashView(SplashScreen.this);
		setContentView(view);

		// set time to splash out
		final int nWelcomeScreenDisplay = 1000;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent mainIntent = new Intent(SplashScreen.this,
						WatchActivity.class);
				startActivity(mainIntent);
				SplashScreen.this.finish();
			}
		}, nWelcomeScreenDisplay);

		// remove cached folds of youmi, domob
		String strSdcardPath = "/mnt/sdcard";
		String[] strFoldNames = {"youmicache", "DomobAppDownload"};
		for (String strFoldName : strFoldNames) {
			String path = strSdcardPath + File.separator + strFoldName;
			Utils.delFolder(path);
		}

		// Utils.delFolder("/mnt/sdcard/.adtwkr/AppCache");
	}

	@Override
	public boolean onKeyUp(int keycode, KeyEvent event) {
		switch (keycode) {

			case KeyEvent.KEYCODE_BACK :
				return true;
		}
		return super.onKeyUp(keycode, event);
	}

	class SplashView extends View {
		SplashView(Context context) {
			super(context);
		}
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			Paint paint = new Paint();
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher_alarmclock);

			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
			int x = displayMetrics.widthPixels / 2 - bitmap.getWidth() / 2;
			int y = displayMetrics.heightPixels / 2 - bitmap.getHeight() / 2;

			canvas.drawBitmap(bitmap, x, y, paint);
		}
	}
}
