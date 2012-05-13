package com.adtworker.mail;

import java.io.File;

import android.app.Activity;
import android.os.Environment;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.adtworker.mail.constants.Constants;
import com.adview.AdViewInterface;
import com.adview.AdViewLayout;
import com.adview.AdViewTargeting;
import com.adview.AdViewTargeting.RunMode;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class Utils {

	/**
	 * Delete a specified folder and its contents
	 * 
	 * @param folderPath
	 * @return void
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

	/**
	 * 设置Adview广告位
	 * 
	 * @param context
	 * @param parent
	 * @param setAdInterface
	 *            是否重写AD接口
	 * @return void
	 */
	public static void setupAdLayout(Activity context, ViewGroup parent,
			boolean setAdInterface) {
		if (android.os.Build.VERSION.SDK_INT < 12 || Constants.ALWAYS_SHOW_AD) {

			/* 下面两行只用于测试,完成后一定要去掉,参考文挡说明 */
			// AdViewTargeting.setUpdateMode(UpdateMode.EVERYTIME); //
			// 保证每次都从服务器取配置
			AdViewTargeting.setRunMode(RunMode.NORMAL); // 保证所有选中的广告公司都为测试状态
			/* 下面这句方便开发者进行发布渠道统计,详细调用可以参考java doc */
			// AdViewTargeting.setChannel(Channel.GOOGLEMARKET);

			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.FILL_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.TOP | Gravity.CENTER;

			AdViewLayout adViewLayout = new AdViewLayout(context,
					"SDK20122309480217x9sp4og4fxrj2ur");
			if (setAdInterface) {
				adViewLayout.setAdViewInterface((AdViewInterface) context);
			}
			parent.addView(adViewLayout);
			parent.invalidate();
		} else if (android.os.Build.VERSION.SDK_INT >= 12) {
			setupAdmobAdView(context, parent);
		}
	}

	/**
	 * 设置Admob广告位
	 * 
	 * @param context
	 * @param parent
	 * @return void
	 */
	public static void setupAdmobAdView(Activity context, ViewGroup parent) {
		AdView adView = new AdView(context, AdSize.BANNER, "a14fab3d9421605");
		parent.addView(adView);
		adView.loadAd(new AdRequest());
	}

	/**
	 * 设置suizong广告位
	 * 
	 * @param context
	 * @param parent
	 * @return void
	 */
	public static void setupSuizongAdView(Activity context, ViewGroup parent) {
		if (android.os.Build.VERSION.SDK_INT < 12 /* || Constants.ALWAYS_SHOW_AD */) {
			com.suizong.mobplate.ads.AdView adView = new com.suizong.mobplate.ads.AdView(
					context, com.suizong.mobplate.ads.AdSize.BANNER,
					"4f46e9bc7c6e1848b8d48e61");
			parent.addView(adView);
			com.suizong.mobplate.ads.AdRequest adRequest = new com.suizong.mobplate.ads.AdRequest();
			adView.loadAd(adRequest);
		}
	}

	public static String getCachedFilename(String url, boolean isThumb) {
		String path = getAppCacheDir();
		if (isThumb) {
			path += File.separator + ".thumbnails";
			File file = new File(path);
			if (!file.exists())
				file.mkdirs();
		}
		path += File.separator + url.hashCode();
		if (isThumb) {
			path = path + ".jpg";
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
