package com.adtworker.mail;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class Utils {

	public static void delFolder(String folderPath) {
		try {
			delAllFileinFolder(folderPath);
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete();

		} catch (Exception e) {
			System.out.println("Error in deleting fold " + folderPath);
			e.printStackTrace();

		}
	}

	public static void delAllFileinFolder(String path) {
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
				delAllFileinFolder(path + "/" + tempList[i]);
				delFolder(path + "/" + tempList[i]);
			}
		}
	}

	public static long getFolderSize(String path) {
		File file = new File(path);
		if (!file.exists())
			return 0;

		if (!file.isDirectory()) {
			return file.length();
		}
		String[] tempList = file.list();
		String tempStr = null;
		long totalSize = 0;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				tempStr = path + tempList[i];
			} else {
				tempStr = path + File.separator + tempList[i];
			}
			totalSize += getFolderSize(tempStr);
		}
		return totalSize;
	}

	public static long getAvailableSize(String path) {
		File pathFile = new File(path);
		android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());
		long nBlocSize = statfs.getBlockSize();
		long nAvailaBlock = statfs.getAvailableBlocks();
		return nAvailaBlock * nBlocSize;
	}

	public static String getAppCacheDir(Context context) {
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
			path = context.getFilesDir().getPath() + File.separator + APP_CACHE;
			file = new File(path);
			if (!file.exists())
				file.mkdir();

			if (!file.exists())
				return null;
		}

		return path;
	}
}
