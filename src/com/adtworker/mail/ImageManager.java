package com.adtworker.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.util.DisplayMetrics;
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
	private final Context mContext;
	private boolean mInitListFailed = false;
	private int mCurrentImageIndex = INVALID_PIC_INDEX;
	private int mLastImageIndex = INVALID_PIC_INDEX;
	private final int[] mCurrentIndexArray = new int[IMAGE_PATH_TYPE.IMAGE_PATH_TYPE_LEN
			.ordinal()];
	private final int[] mLastIndexArray = new int[IMAGE_PATH_TYPE.IMAGE_PATH_TYPE_LEN
			.ordinal()];
	private int mBitmapCacheCurrent = 0;
	private final int mSearchPageNum = 2;
	private final int mSearchPageSize = 8;
	private String mQueryKeyword;
	private final Bitmap[] mBitmapCache = new Bitmap[2];
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

	public IMAGE_PATH_TYPE getImagePathType() {
		return mImagePathType;
	}

	public boolean isInitListFailed() {
		return mInitListFailed;
	}

	public void setQueryKeyword(String word) {
		mQueryKeyword = word;
	}

	public int getImageListSize() {
		return mImageList.size();
	}

	private boolean isValidIndex(int index) {
		if (index < 0 || index >= getImageListSize())
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
		mCurrentImageIndex = getNextSteppedIndex(step);
		return getCurrentStr();
	}

	public int getCurrent() {
		return mCurrentImageIndex;
	}

	public int getNext() {
		return getNextSteppedIndex(1);
	}

	public int getPrev() {
		return getNextSteppedIndex(-1);
	}

	public int getNextSteppedIndex(int step) {
		int size = getImageListSize();
		return (size + mCurrentImageIndex + step) % size;
	}

	public Bitmap getCurrentBitmap() {
		if (mCurrentBitmap == null) {
			try {
				if (mImagePathType == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
					InputStream is = mContext.getAssets().open(getCurrentStr());
					mBitmapCache[mBitmapCacheCurrent] = BitmapFactory
							.decodeStream(is);
				} else if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
					new loadImageTask().execute(getCurrentStr());

					DisplayMetrics displayMetrics = mContext.getResources()
							.getDisplayMetrics();
					int width = displayMetrics.widthPixels;
					int height = displayMetrics.heightPixels;
					Bitmap bm = Bitmap.createBitmap(width, height,
							Bitmap.Config.ARGB_8888);
					Canvas canvas = new Canvas(bm);
					Bitmap bmIcon = BitmapFactory.decodeResource(
							mContext.getResources(),
							R.drawable.ic_launcher_alarmclock);
					canvas.drawBitmap(bmIcon, (width - bmIcon.getWidth()) / 2,
							(height - bmIcon.getHeight()) / 2, null);
					bmIcon.recycle();
					mBitmapCache[mBitmapCacheCurrent] = bm;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			mCurrentBitmap = mBitmapCache[mBitmapCacheCurrent];
		}
		return mCurrentBitmap;
	}
	public Bitmap getImageBitmap(int step) {
		int nextImageIndex = getNextSteppedIndex(step);
		mBitmapCacheCurrent = (mBitmapCacheCurrent + 1) % mBitmapCache.length;

		if (nextImageIndex != mLastImageIndex) {
			mLastImageIndex = mCurrentImageIndex;
			if (mBitmapCache[mBitmapCacheCurrent] != null) {
				mBitmapCache[mBitmapCacheCurrent].recycle();
				mBitmapCache[mBitmapCacheCurrent] = null;
			}
			try {
				if (mImagePathType == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
					InputStream is = mContext.getAssets().open(
							getImageStr(step));
					mBitmapCache[mBitmapCacheCurrent] = BitmapFactory
							.decodeStream(is);
				} else if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
					new loadImageTask().execute(getImageStr(step));

					DisplayMetrics displayMetrics = mContext.getResources()
							.getDisplayMetrics();
					int width = displayMetrics.widthPixels;
					int height = displayMetrics.heightPixels;
					Bitmap bm = Bitmap.createBitmap(width, height,
							Bitmap.Config.ARGB_8888);
					Canvas canvas = new Canvas(bm);
					Bitmap bmIcon = BitmapFactory.decodeResource(
							mContext.getResources(),
							R.drawable.ic_launcher_alarmclock);
					canvas.drawBitmap(bmIcon, (width - bmIcon.getWidth()) / 2,
							(height - bmIcon.getHeight()) / 2, null);
					bmIcon.recycle();
					mBitmapCache[mBitmapCacheCurrent] = bm;
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
		mInitListFailed = true;
		switch (mImagePathType) {
			case LOCAL_ASSETS :
				ArrayList<String> arrayList = getAssetsImagesList(IMAGE_SUBFOLDER_IN_ASSETS);
				for (int i = 0; i < arrayList.size(); i++) {
					mImageList.add(arrayList.get(i));
				}
				mInitListFailed = false;
				break;

			case REMOTE_HTTP_URL :
				new getImagesTask().execute();
				break;
		}

		mCurrentImageIndex = mCurrentIndexArray[mImagePathType.ordinal()];
		mLastImageIndex = mLastIndexArray[mImagePathType.ordinal()];
	}
	private class getImagesTask extends AsyncTask<Void, Void, Void> {
		WatchActivity activity = (WatchActivity) mContext;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.mProgressBar.setProgress(0);
			activity.mProgressBar.setVisibility(View.VISIBLE);
			activity.mProgressBar.setMax(mSearchPageNum * mSearchPageSize);
			activity.EnableNextPrevButtons(false);
		}
		@Override
		protected Void doInBackground(Void... params) {
			try {
				for (int i = 0; i < mSearchPageNum; i++) {
					String keyword = Uri.encode(mQueryKeyword, "GBK");
					// List<String> temp = GoogleImage.getImgUrl(keyword, i
					// * mSearchPageSize, mSearchPageSize);
					List<String> temp = BaiduImage.getImgUrl(keyword, i
							* mSearchPageSize, mSearchPageSize, 480, 800);
					for (int j = 0; j < temp.size(); j++) {
						mImageList.add(temp.get(j));
						activity.mProgressBar.setProgress(i * temp.size() + j
								+ 1);
					}
				}
				activity.mProgressBar.setProgress(mSearchPageNum
						* mSearchPageSize);
				mInitListFailed = false;
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL
					&& getImageListSize() == 0) {
				Log.e(TAG, "remote update fails, back to local mode.");
				setImagePathType(IMAGE_PATH_TYPE.LOCAL_ASSETS);
			}

			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			activity.EnableNextPrevButtons(true);
			SystemClock.sleep(200);
			activity.mProgressBar.setVisibility(View.GONE);
		}
	}

	private class loadImageTask extends AsyncTask<String, Integer, Bitmap> {
		WatchActivity activity = (WatchActivity) mContext;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.mProgressIcon.setVisibility(View.VISIBLE);
			activity.EnableNextPrevButtons(false);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			publishProgress(0);
			final Bitmap bm;
			String cachedFileString = getCachedFilename(params[0]);
			File file = new File(cachedFileString);
			if (file.exists()) {
				Log.d(TAG, "file exists: " + cachedFileString);
				try {
					InputStream is = new BufferedInputStream(
							new FileInputStream(file));
					bm = BitmapFactory.decodeStream(is);
					publishProgress(100);
					return bm;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			try {
				URLConnection connection = new URL(params[0]).openConnection();
				String contentType = connection.getHeaderField("Content-Type");
				boolean isImage = contentType.startsWith("image/");
				publishProgress(30);
				if (isImage) {
					HttpGet httpRequest = new HttpGet(params[0]);
					HttpClient httpclient = new DefaultHttpClient();
					HttpResponse response = httpclient.execute(httpRequest);
					publishProgress(70);
					HttpEntity entity = response.getEntity();
					BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(
							entity);
					InputStream is = bufferedHttpEntity.getContent();
					bm = BitmapFactory.decodeStream(is);
					SaveImageFile(bufferedHttpEntity.getContent(), params[0]);
				} else {
					bm = BitmapFactory.decodeResource(mContext.getResources(),
							R.drawable.no_image);
				}
			} catch (Exception e) {
				Log.e(e.getClass().getName(), e.getMessage(), e);
				return null;
			}
			publishProgress(100);
			return bm;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			activity.mProgressIcon.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (result != null) {
				activity.setImageView(result);
				mBitmapCache[mBitmapCacheCurrent] = result;
			}
			activity.mProgressIcon.setVisibility(View.GONE);
			activity.EnableNextPrevButtons(true);
		}
	}

	private void SaveImageFile(InputStream is, String str) {

		Log.d(TAG, "Saving " + getCachedFilename(str));
		File file = new File(getCachedFilename(str));
		OutputStream out = null;
		if (file.exists())
			return;

		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			byte[] buf = new byte[1024];
			while (is.read(buf) != -1) {
				out.write(buf);
			}
			out.flush();

			if (out != null)
				out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String getCachedFilename(String imgPath) {
		// String path = mContext.getFilesDir().getPath();
		String path = Environment.getExternalStorageDirectory()
				+ File.separator + "iWatch";

		File file = new File(path);
		if (!file.exists())
			file.mkdirs();

		return path + File.separator + getHostname(imgPath) + "."
				+ getImageFilename(imgPath);
	}

	private String getHostname(String imgPath) {
		int start = imgPath.indexOf("/") + 1;
		start = imgPath.indexOf("/", start) + 1;
		int end = imgPath.indexOf("/", start);
		if (start != -1 && end != -1)
			return imgPath.substring(start, end);
		else
			return null;
	}

	private String getImageFilename(String imgPath) {
		int start = imgPath.lastIndexOf("/");
		int end = imgPath.lastIndexOf("?");
		if (start != -1 && end == -1) {
			return imgPath.substring(start + 1);
		} else if (start != -1 && end != -1) {
			return imgPath.substring(start + 1, end - 1);
		} else {
			return null;
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

}
