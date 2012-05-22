package com.adtworker.mail.util;

import java.io.File;

import android.os.Environment;
import android.util.Log;

import com.adtworker.mail.WatchApp;
import com.adtworker.mail.constants.Constants;

public class FileUtils {

	/**
	 * remove cached folds of youmi, domob and so on
	 */
	public static void clearAdCache() {
		delApkFile("/sdcard/Download");
		delFolder("/sdcard/ad");
		delFolder("/sdcard/adwo");
		delFolder("/sdcard/logger");
		delFolder("/sdcard/Tencent/MobWIN");
		delFolder("/sdcard/DomobAppDownload");
		delFolder("/sdcard/youmicache");
		delFolder("/sdcard/logger");
		delFolder("/sdcard/app_dump");
		delFolder("/sdcard/UCDownloads");

	}
	public static void delFolder(String floder) {
		File f = new File(floder);
		File[] fl = f.listFiles();
		if (fl == null) {
			return;
		}
		for (int i = 0; i < fl.length; i++) {
			Log.d(Constants.TAG, "del file path: " + fl[i].toString());
			if (fl[i].isDirectory()) {
				delFolder(fl[i].getAbsolutePath());
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

	/**
	 * get available size of specified mount partition
	 * 
	 * @param path
	 * @return long
	 */
	public static long getAvailableSize(String path) {
		File pathFile = new File(path);
		android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());
		long nBlocSize = statfs.getBlockSize();
		long nAvailaBlock = statfs.getAvailableBlocks();
		return nAvailaBlock * nBlocSize;
	}

	/**
	 * get App's cache folder path
	 * 
	 * @param folderPath
	 * @return String
	 */
	public static String getAppCacheDir() {
		final String APP_SUBFOLDER = ".adtwkr";
		final String APP_CACHE = "AppCache";
		String path = Environment.getExternalStorageDirectory()
				+ File.separator + APP_SUBFOLDER + File.separator + APP_CACHE;

		File file = new File(path);
		if (!file.exists())
			file.mkdirs();

		if (!file.exists()) {
			path = WatchApp.getInstance().getFilesDir().getPath()
					+ File.separator + APP_CACHE;
			file = new File(path);
			if (!file.exists())
				file.mkdir();

			if (!file.exists())
				return null;
		}

		return path;
	}

	public static String getCachedFilename(String url, boolean isThumb) {
		String path = getAppCacheDir();
		if (isThumb) {
			path += File.separator + ".thumbnails";
			File file = new File(path);
			if (!file.exists())
				file.mkdirs();
		}
		path += File.separator + Integer.toHexString(url.hashCode());

		if (isThumb) {
			String ext = url.substring(url.lastIndexOf("."));
			if (ext.startsWith("."))
				path += ext;
			else
				path += ".jpg";
		}
		return path;
	}
	public static String getTempCachedFilename(String url, boolean isThumb) {
		return getCachedFilename(url, isThumb) + "~";
	}

	public static File getFile(String url, boolean isThumb) {
		File file = null;
		try {
			file = new File(getCachedFilename(url, isThumb));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	public static File getTempFile(String url, boolean isThumb) {
		File file = null;
		try {
			file = new File(getTempCachedFilename(url, isThumb));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	public static boolean renameTempFile(String url, boolean isThumb) {
		File file = new File(getTempCachedFilename(url, isThumb));
		if (file.exists()) {
			return file.renameTo(new File(getCachedFilename(url, isThumb)));
		}
		return false;
	}
}
