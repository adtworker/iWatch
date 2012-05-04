package com.adtworker.mail;

import android.app.Application;

public class WatchApp extends Application {

	private static WatchApp instance;

	public static WatchApp getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		instance = this;
	}

}
