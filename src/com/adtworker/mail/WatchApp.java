package com.adtworker.mail;

import android.app.Application;
import android.util.Log;

import com.adtworker.mail.constants.Constants;

public class WatchApp extends Application {

	private static WatchApp mWatchAppInstance = null;
	private static ImageManager mImageManager = null;
	private static DownloadManager mDownloadManager = null;

	public static WatchApp getInstance() {
		return mWatchAppInstance;
	}

	public static ImageManager getImageManager() {
		if (mImageManager == null)
			mImageManager = ImageManager.getInstance();

		return mImageManager;
	}

	public static DownloadManager getDownloadManager() {
		if (mDownloadManager == null) {
			mDownloadManager = DownloadManager.getInstance();
			mDownloadManager.start();
		}
		return mDownloadManager;
	}

	@Override
	public void onCreate() {
		Log.d(Constants.TAG, "WatchApp onCreate()");
		super.onCreate();
		mWatchAppInstance = this;
		mImageManager = ImageManager.getInstance();
		mDownloadManager = DownloadManager.getInstance();
		mDownloadManager.start();
	}

	public void recycle() {
		if (mImageManager != null) {
			mImageManager.recycle();
			mImageManager = null;
		}

		if (mDownloadManager != null) {
			mDownloadManager.recycle();
			mDownloadManager = null;
		}
	}

}
