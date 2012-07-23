package com.adtworker.mail;

import java.io.File;

import android.os.Environment;

public class Utils {

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
			// Environment.getExternalStorageState() !=
			// Environment.MEDIA_MOUNTED
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
