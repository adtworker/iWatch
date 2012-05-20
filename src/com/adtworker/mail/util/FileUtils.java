package com.adtworker.mail.util;

import java.io.File;

import android.util.Log;

import com.adtworker.mail.constants.Constants;

public class FileUtils {

	/**
	 * remove cached folds of youmi, domob and so on
	 */
	public static void clearAdCache() {
		delApkFile("/sdcard/Download");
		delFloder("/sdcard/ad");
		delFloder("/sdcard/adwo");
		delFloder("/sdcard/logger");
		delFloder("/sdcard/Tencent/MobWIN");
		delFloder("/sdcard/DomobAppDownload");
		delFloder("/sdcard/youmicache");
		delFloder("/sdcard/logger");
		delFloder("/sdcard/app_dump");
		delFloder("/sdcard/UCDownloads");

	}
	public static void delFloder(String floder) {
		File f = new File(floder);
		File[] fl = f.listFiles();
		if (fl == null) {
			return;
		}
		for (int i = 0; i < fl.length; i++) {
			Log.d(Constants.TAG, "del file path: " + fl[i].toString());
			if (fl[i].isDirectory()) {
				delFloder(fl[i].getAbsolutePath());
			} else {
				fl[i].delete();
				Log.d(Constants.TAG, fl[i].toString() + " is del");
			}
		}
	}

	public static void delApkFile(String path) {
		File f = new File(path);
		File[] fl = f.listFiles();
		if (fl == null) {
			return;
		}
		for (int i = 0; i < fl.length; i++) {
			if (fl[i].isDirectory()) {
				delApkFile(fl[i].getAbsolutePath());
			} else {
				if (fl[i].toString().toLowerCase().endsWith("apk")) {
					fl[i].delete();
					Log.e("test", fl[i].toString() + " is del");
				}
			}
		}
	}

	/**
	 * get total size of specified folder
	 * 
	 * @param path
	 * @return long
	 */
	public static long getFolderSize(String path) {
		File file = new File(path);
		if (!file.exists())
			return 0;

		if (!file.isDirectory()) {
			return file.length();
		}
		File[] tempListFiles = file.listFiles();
		long totalSize = 0;
		for (int i = 0; i < tempListFiles.length; i++) {
			totalSize += getFolderSize(tempListFiles[i].getAbsolutePath());
		}
		return totalSize;
	}
}
