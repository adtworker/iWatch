package com.adtworker.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;

public class ImageManager {

	public enum IMAGE_PATH_TYPE {
		LOCAL_ASSETS, // assets/
		PHONE_STORAGE, // /mnt/sdcard
		EXTERNAL_SD_CARD, // /mnt/sdcard-ext
		PACKAGE_PRIVATE, // /data/data/$package_name
		REMOTE_HTTP_URL // http://xxx
	}

	private final String TAG = "ImageManager";
	private final String IMAGE_SUBFOLDER_IN_ASSETS = "pics";
	private Context mContext;
	private final Random mRandom = new Random(System.currentTimeMillis());
	private int mCurrentImageIndex = INVALID_PIC_INDEX;

	public final static int INVALID_PIC_INDEX = -1;

	public IMAGE_PATH_TYPE mImagePathType = IMAGE_PATH_TYPE.LOCAL_ASSETS;
	public ArrayList<String> mImageList = new ArrayList<String>();

	public ImageManager(Context context) {
		mContext = context;
		initImageList();

	}

	private boolean isValidIndex(int index) {
		if (index < 0 || index >= mImageList.size())
			return false;
		else
			return true;
	}
	public void setCurrent(int index) {
		if (isValidIndex(index)
				&& mImagePathType != IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {

			mCurrentImageIndex = index;
		}
	}

	public String getCurrentStr() {
		return mImageList.get(mCurrentImageIndex);
	}

	public String getImageStr(int step) {
		mCurrentImageIndex = (mCurrentImageIndex + step) % mImageList.size();
		return getCurrentStr();
	}

	public int getCurrent() {
		return mCurrentImageIndex;
	}

	public int getNext() {
		return (mCurrentImageIndex + 1) % mImageList.size();
	}

	public int getPrev() {
		return (mCurrentImageIndex - 1) % mImageList.size();
	}

	private void initImageList() {
		ArrayList<String> arrayList = getAssetsPicsList(IMAGE_SUBFOLDER_IN_ASSETS);
		for (int i = 0; i < arrayList.size(); i++) {
			mImageList.add(arrayList.get(i));
		}
		mCurrentImageIndex = mRandom.nextInt(mImageList.size());
	}

	private void unzipFile(String targetPath, String zipFilePath,
			boolean isAssets) {

		try {
			File zipFile = new File(zipFilePath);
			InputStream is;
			if (isAssets) {
				is = mContext.getAssets().open(zipFilePath);
			} else {
				is = new FileInputStream(zipFile);
			}
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				String zipPath = entry.getName();
				try {

					if (entry.isDirectory()) {
						File zipFolder = new File(targetPath + File.separator
								+ zipPath);
						if (!zipFolder.exists()) {
							zipFolder.mkdirs();
						}
					} else {
						File file = new File(targetPath + File.separator
								+ zipPath);
						if (!file.exists()) {
							File pathDir = file.getParentFile();
							pathDir.mkdirs();
							file.createNewFile();
						}

						FileOutputStream fos = new FileOutputStream(file);
						int bread;
						while ((bread = zis.read()) != -1) {
							fos.write(bread);
						}
						fos.close();

					}

				} catch (Exception e) {
					continue;
				}
			}
			zis.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private boolean isValidPic(File file) {
		if (file.isFile()) {
			String fileName = file.getName();
			return isValidPic(fileName);
		}
		return false;
	}

	private boolean isValidPic(String filename) {
		filename = filename.toLowerCase();
		if (filename.endsWith(".jpg") || filename.endsWith(".png")
				|| filename.endsWith(".bmp")) {
			return true;
		}
		return false;
	}

	private ArrayList<String> getPicsList(String path) {
		ArrayList<String> arrayList = new ArrayList<String>();
		File[] files = new File(path).listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				ArrayList<String> tmpArrayList = getPicsList(path
						+ File.separator + file.getName());
				for (int i = 0; i < tmpArrayList.size(); i++) {
					arrayList.add(file.getName() + File.separator
							+ tmpArrayList.get(i));
				}
			}
			if (isValidPic(file)) {
				arrayList.add(file.getName());
			}
		}
		return arrayList;
	}

	private ArrayList<String> getAssetsPicsList(String path) {
		if (path == null)
			path = "";

		ArrayList<String> arrayList = new ArrayList<String>();
		try {
			String[] filenames = mContext.getAssets().list(path);
			for (int i = 0; i < filenames.length; i++) {
				String filepath = path + File.separator + filenames[i];
				if (isValidPic(filepath))
					arrayList.add(filepath);
				else {
					ArrayList<String> tmp = getAssetsPicsList(filepath);
					for (int j = 0; j < tmp.size(); j++)
						arrayList.add(tmp.get(j));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return arrayList;
	}
}
