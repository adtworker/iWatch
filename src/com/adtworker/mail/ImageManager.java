package com.adtworker.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class ImageManager {

	public enum IMAGE_PATH_TYPE {
		LOCAL_ASSETS, // assets/
		PHONE_STORAGE, // /mnt/sdcard
		EXTERNAL_SD_CARD, // /mnt/sdcard-ext
		PACKAGE_PRIVATE, // /data/data/$package_name
		REMOTE_HTTP_URL, // http://xxx
		IMAGE_PATH_TYPE_LEN
	}

	private final String TAG = "ImageManager";
	private final String IMAGE_SUBFOLDER_IN_ASSETS = "pics";
	private Context mContext;
	private int mCurrentImageIndex = INVALID_PIC_INDEX;
	private int mLastImageIndex = INVALID_PIC_INDEX;
	private int[] mCurrentIndexArray = new int[IMAGE_PATH_TYPE.IMAGE_PATH_TYPE_LEN
			.ordinal()];
	private int[] mLastIndexArray = new int[IMAGE_PATH_TYPE.IMAGE_PATH_TYPE_LEN
			.ordinal()];
	private int mBitmapCacheCurrent = 0;
	private int mSearchPageNum = 10;
	private int mSearchPageSize = 8;
	private String mQueryKeyword;
	private Bitmap[] mBitmapCache = new Bitmap[2];
	private Bitmap mCurrentBitmap = null;

	public final static int INVALID_PIC_INDEX = -1;

	public IMAGE_PATH_TYPE mImagePathType = IMAGE_PATH_TYPE.LOCAL_ASSETS;
	public ArrayList<String> mImageList = new ArrayList<String>();

	public ImageManager(Context context) {
		mContext = context;
		for (int i = 0; i < mCurrentIndexArray.length; i++)
			mCurrentIndexArray[i] = INVALID_PIC_INDEX;
		for (int i = 0; i < mLastIndexArray.length; i++)
			mLastIndexArray[i] = INVALID_PIC_INDEX;

		setImagePathType(mImagePathType);
	}
	public void setImagePathType(IMAGE_PATH_TYPE type) {
		mCurrentIndexArray[mImagePathType.ordinal()] = mCurrentImageIndex;
		mLastIndexArray[mImagePathType.ordinal()] = mLastImageIndex;
		mImagePathType = type;
		initImageList();
	}

	public void setQueryKeyword(String word) {
		mQueryKeyword = word;
	}

	public IMAGE_PATH_TYPE getImagePathType() {
		return mImagePathType;
	}

	private boolean isValidIndex(int index) {
		if (index < 0 || index >= mImageList.size())
			return false;
		else
			return true;
	}
	public void setCurrent(int index) {
		if (isValidIndex(index)) {
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

	public Bitmap getCurrentBitmap() {
		if (mCurrentBitmap == null) {
			try {
				if (mImagePathType == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
					InputStream is = mContext.getAssets().open(getCurrentStr());
					mBitmapCache[mBitmapCacheCurrent] = BitmapFactory
							.decodeStream(is);
				} else if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
					new loadImageTask().execute();
					mBitmapCache[mBitmapCacheCurrent] = BitmapFactory
							.decodeResource(mContext.getResources(),
									R.drawable.ic_launcher_alarmclock);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			mCurrentBitmap = mBitmapCache[mBitmapCacheCurrent];
		}
		return mCurrentBitmap;
	}

	public Bitmap getImageBitmap(int step) {
		int nextImageIndex = (mCurrentImageIndex + step) % mImageList.size();
		mBitmapCacheCurrent = (mBitmapCacheCurrent + 1) % mBitmapCache.length;

		if (nextImageIndex != mLastImageIndex) {
			mLastImageIndex = mCurrentImageIndex;
			if (mBitmapCache[mBitmapCacheCurrent] != null) {
				// mBitmapCache[mBitmapCacheCurrent].recycle();
			}
			try {
				if (mImagePathType == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
					InputStream is = mContext.getAssets().open(
							getImageStr(step));
					mBitmapCache[mBitmapCacheCurrent] = BitmapFactory
							.decodeStream(is);
				} else if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
					getImageStr(step);
					new loadImageTask().execute();
					mBitmapCache[mBitmapCacheCurrent] = BitmapFactory
							.decodeResource(mContext.getResources(),
									R.drawable.ic_launcher_alarmclock);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			mLastImageIndex = mCurrentImageIndex;
			mCurrentImageIndex = nextImageIndex;
		}
		mCurrentBitmap = mBitmapCache[mBitmapCacheCurrent];
		return mCurrentBitmap;
	}
	private void initImageList() {
		mImageList.clear();
		switch (mImagePathType) {
			case LOCAL_ASSETS :
				ArrayList<String> arrayList = getAssetsImagesList(IMAGE_SUBFOLDER_IN_ASSETS);
				for (int i = 0; i < arrayList.size(); i++) {
					mImageList.add(arrayList.get(i));
				}
				break;

			case REMOTE_HTTP_URL :
				new getImagesTask().execute();
				break;
		}

		mCurrentImageIndex = mCurrentIndexArray[mImagePathType.ordinal()];
		mLastImageIndex = mLastIndexArray[mImagePathType.ordinal()];
	}

	private class getImagesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			WatchActivity activity = (WatchActivity) mContext;
			activity.mProgressBar.setProgress(0);
			activity.mProgressBar.setVisibility(View.VISIBLE);
			activity.mProgressBar.setMax(mSearchPageNum * mSearchPageSize);
			activity.EnableNextPrevButtons(false);
		}
		@Override
		protected Void doInBackground(Void... params) {
			try {
				for (int i = 0; i < mSearchPageNum; i++) {
					String keyword = Uri.encode(mQueryKeyword);
					List<String> temp = GoogleImage.getImgUrl(keyword, i
							* mSearchPageSize, mSearchPageSize);
					for (int j = 0; j < temp.size(); j++) {
						mImageList.add(temp.get(j));
						((WatchActivity) mContext).mProgressBar.setProgress(i
								* temp.size() + j + 1);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Log.v(TAG, "size is " + mImageList.size());

			if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL
					&& mImageList.size() == 0) {
				Log.e(TAG, "remote update fails, back to local mode.");
				setImagePathType(IMAGE_PATH_TYPE.LOCAL_ASSETS);
			}

			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			((WatchActivity) mContext).mProgressBar.setVisibility(View.GONE);
			((WatchActivity) mContext).EnableNextPrevButtons(true);
		}
	}

	private class loadImageTask extends AsyncTask<Void, Void, Void> {
		Bitmap bm;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			WatchActivity activity = (WatchActivity) mContext;
			activity.EnableNextPrevButtons(false);
		}
		@Override
		protected Void doInBackground(Void... params) {
			bm = LoadImageFromURL(getCurrentStr());
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			WatchActivity activity = (WatchActivity) mContext;
			if (bm != null) {
				activity.setImageView(bm);
			}
			activity.EnableNextPrevButtons(true);
		}
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

	private boolean isValidImage(File file) {
		if (file.isFile()) {
			String fileName = file.getName();
			return isValidImage(fileName);
		}
		return false;
	}

	private boolean isValidImage(String filename) {
		filename = filename.toLowerCase();
		if (filename.endsWith(".jpg") || filename.endsWith(".png")
				|| filename.endsWith(".bmp")) {
			return true;
		}
		return false;
	}

	private ArrayList<String> getImagesList(String path) {
		ArrayList<String> arrayList = new ArrayList<String>();
		File[] files = new File(path).listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				ArrayList<String> tmpArrayList = getImagesList(path
						+ File.separator + file.getName());
				for (int i = 0; i < tmpArrayList.size(); i++) {
					arrayList.add(file.getName() + File.separator
							+ tmpArrayList.get(i));
				}
			}
			if (isValidImage(file)) {
				arrayList.add(file.getName());
			}
		}
		return arrayList;
	}

	private ArrayList<String> getAssetsImagesList(String path) {
		if (path == null)
			path = "";

		ArrayList<String> arrayList = new ArrayList<String>();
		try {
			String[] filenames = mContext.getAssets().list(path);
			for (int i = 0; i < filenames.length; i++) {
				String filepath = path + File.separator + filenames[i];
				if (isValidImage(filepath))
					arrayList.add(filepath);
				else {
					ArrayList<String> tmp = getAssetsImagesList(filepath);
					for (int j = 0; j < tmp.size(); j++)
						arrayList.add(tmp.get(j));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return arrayList;
	}

	private Bitmap LoadImageFromURL(String url) {
		try {
			URLConnection connection = new URL(url).openConnection();
			String contentType = connection.getHeaderField("Content-Type");
			boolean isImage = contentType.startsWith("image/");
			if (isImage) {
				HttpGet httpRequest = new HttpGet(url);
				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = httpclient.execute(httpRequest);
				HttpEntity entity = response.getEntity();
				BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(
						entity);

				InputStream is = bufferedHttpEntity.getContent();
				Bitmap bm = BitmapFactory.decodeStream(is);
				return bm;
			} else {
				Bitmap bm = BitmapFactory.decodeResource(
						mContext.getResources(), R.drawable.no_image);
				return bm;
			}
		} catch (Exception e) {
			Log.e(e.getClass().getName(), e.getMessage(), e);
			return null;
		}
	}
}
