package com.adtworker.mail;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.view.ViewGroup;

import com.adtworker.mail.constants.Constants;
import com.adview.AdViewInterface;
import com.adview.AdViewLayout;
import com.adview.AdViewTargeting;
import com.adview.AdViewTargeting.RunMode;

public class Utils {

	/**
	 * Delete a specified folder and its contents
	 * 
	 * @param String
	 *            folderPath
	 * @return
	 */
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

	protected static void delAllFileinFolder(String path) {
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

	/**
	 * get total size of specified folder
	 * 
	 * @param String
	 *            path
	 * @return
	 */
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

	/**
	 * get available size of specified mount partition
	 * 
	 * @param String
	 *            path
	 * @return
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
	 * @param String
	 *            folderPath
	 * @return String
	 */
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

	/**
	 * 设置广告位
	 * 
	 * @param Context
	 *            context
	 * @param ViewGroup
	 *            parent
	 * @return
	 */
	@SuppressWarnings("unused")
	public static void setupAdLayout(Context context, ViewGroup parent) {
		if (android.os.Build.VERSION.SDK_INT >= 12
				&& !(Constants.ALWAYS_SHOW_AD))
			return;

		/* 下面两行只用于测试,完成后一定要去掉,参考文挡说明 */
		// AdViewTargeting.setUpdateMode(UpdateMode.EVERYTIME); //
		// 保证每次都从服务器取配置
		AdViewTargeting.setRunMode(RunMode.NORMAL); // 保证所有选中的广告公司都为测试状态
		/* 下面这句方便开发者进行发布渠道统计,详细调用可以参考java doc */
		// AdViewTargeting.setChannel(Channel.GOOGLEMARKET);
		AdViewLayout adViewLayout = new AdViewLayout((Activity) context,
				"SDK20122309480217x9sp4og4fxrj2ur");
		adViewLayout.setAdViewInterface((AdViewInterface) context);
		parent.addView(adViewLayout);
		parent.invalidate();
	}
}
