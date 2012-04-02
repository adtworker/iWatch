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
import android.view.View;

public class SplashScreen extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = new SplashView(SplashScreen.this);
		setContentView(view);

		// set time to splash out
		final int nWelcomeScreenDisplay = 2000;
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
			delFolder(path);
		}
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

	public void delFolder(String folderPath) {
		try {
			delAllFile(folderPath);
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete();

		} catch (Exception e) {
			System.out.println("Error in deleting fold " + folderPath);
			e.printStackTrace();

		}
	}

	public void delAllFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);
				delFolder(path + "/" + tempList[i]);
			}
		}
	}

}
