package com.adtworker.mail;

import android.app.Application;

public class WatchApp extends Application {

	private static WatchApp mWatchAppInstance = null;
	private static ImageManager mImageManager = null;
	private static DownloadManager mDownloadManager = null;

	public static WatchApp getInstance() {
		return mWatchAppInstance;
	}

	public static ImageManager getImageManager() {
		return mImageManager;
	}

	public static DownloadManager getDownloadManager() {
		return mDownloadManager;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mWatchAppInstance = this;
		mImageManager = ImageManager.getInstance();
		mDownloadManager = DownloadManager.getInstance();
		mDownloadManager.start();
	}

	public void recycle() {
		if (mImageManager != null)
			mImageManager.recycle();
	}

}
