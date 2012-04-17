package com.adtworker.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
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
	private final int mSearchPageNum = 8;
	private final int mSearchPageSize = 8;
	private Bitmap mCurrentBitmap = null;

	public final static int INVALID_PIC_INDEX = -1;

	public IMAGE_PATH_TYPE mImagePathType = IMAGE_PATH_TYPE.LOCAL_ASSETS;
	public ArrayList<AdtImage> mImageList = new ArrayList<AdtImage>();
	private final ArrayList<String> mQueryKeywords = new ArrayList<String>();

	private static ImageManager mImageManager = null;
	public static ImageManager getInstance(Context context) {
		if (null == mImageManager && null != context) {
			mImageManager = new ImageManager(context);
		}
		return mImageManager;
	}

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

	public void setQueryKeyword(String args) {
		mQueryKeywords.clear();
		String[] words = args.split(" ");
		for (String word : words) {
			mQueryKeywords.add(word);
		}
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

	public boolean isCurrentAsset() {
		return mImageList.get(mCurrentImageIndex).isAsset;
	}

	public String getCurrentStr() {
		return mImageList.get(mCurrentImageIndex).urlFull;
	}

	public String getCurrentStrTb() {
		return mImageList.get(mCurrentImageIndex).urlThumb;
	}

	public String getCurrentStrLocal() {
		return getCachedFilename(mImageList.get(mCurrentImageIndex).urlFull);
	}

	public String getImageStr(int step) {
		mLastImageIndex = mCurrentImageIndex;
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

	private Bitmap getBitmap(String strImage) {
		Bitmap bitmap = null;
		try {
			if (mImagePathType == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
				InputStream is = mContext.getAssets().open(strImage);
				bitmap = BitmapFactory.decodeStream(is);
			} else if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
				bitmap = getBitmapFromSDCard(strImage);

				if (bitmap == null) {
					String tbUrl = getCurrentStrTb();
					Bitmap bm = getBitmapFromSDCard(tbUrl);
					if (bm == null) {
						bm = getBitmapFromUrl(tbUrl);
					}
					new loadImageTask().execute(strImage,
							Integer.toString(mCurrentImageIndex));
					bitmap = bm;
				} else {
					((WatchActivity) mContext).mProgressIcon
							.setVisibility(View.GONE);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public Bitmap getCurrentBitmap() {
		if (mCurrentBitmap == null) {
			String imgstr = getCurrentStr();
			mCurrentBitmap = getBitmap(imgstr);
		}
		return mCurrentBitmap;
	}

	public Bitmap getImageBitmap(int step) {
		String imgstr = getImageStr(step);
		mCurrentBitmap = getBitmap(imgstr);
		return mCurrentBitmap;
	}

	private void initImageList() {
		mImageList.clear();
		mInitListFailed = true;
		switch (mImagePathType) {
			case LOCAL_ASSETS :
				ArrayList<String> arrayList = getAssetsImagesList(IMAGE_SUBFOLDER_IN_ASSETS);
				for (int i = 0; i < arrayList.size(); i++) {
					AdtImage image = new AdtImage(arrayList.get(i), true);
					mImageList.add(image);
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
			activity.mProgressBar.setMax(1000);
			activity.EnableNextPrevButtons(false);
		}
		@Override
		protected Void doInBackground(Void... params) {
			try {
				int count = 0;
				for (int k = 0; k < mQueryKeywords.size(); k++) {
					Log.d(TAG, "querying word " + mQueryKeywords.get(k));
					String keyword = Uri.encode(mQueryKeywords.get(k), "GBK");

					for (int i = 0; i < mSearchPageNum; i++) {

						List<AdtImage> temp = ImageSearchAdapter.getImgList(
								keyword, 480, 800, i * mSearchPageSize,
								mSearchPageSize);

						for (int j = 0; j < temp.size(); j++) {
							activity.mProgressBar.setProgress(++count);
							AdtImage img = temp.get(j);
							String url = img.urlFull;
							if (url.toLowerCase().endsWith(".gif"))
								continue;

							img.urlFull = Uri.decode(img.urlFull);
							img.urlThumb = Uri.decode(img.urlThumb);
							mImageList.add(img);
						}
					}
				}
				Log.d(TAG, "image list size = " + mImageList.size());

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

			if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
				new loadAllImageTask().execute();
			}
		}
	}

	private class loadAllImageTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			if (getImageListSize() == 0)
				return null;

			for (int i = 0; i < mImageList.size(); i++) {
				AdtImage img = mImageList.get(i);
				String url;
				if (img.hasThumb) {
					url = img.urlThumb;
				} else {
					url = img.urlFull;
				}

				Bitmap bitmap = getBitmapFromSDCard(url);
				if (bitmap == null) {
					bitmap = getBitmapFromUrl(url);
					if (bitmap != null) {
						writeBitmap2AppCache(bitmap, url);
					} else {
						Log.e(TAG, "image " + i + " is null.");
					}
				}
			}
			return null;
		}

	}

	private class loadImageTask extends AsyncTask<String, Integer, Bitmap> {
		WatchActivity activity = (WatchActivity) mContext;
		int loadingImgID = INVALID_PIC_INDEX;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.mProgressIcon.setVisibility(View.VISIBLE);
			// activity.EnableNextPrevButtons(false);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			loadingImgID = Integer.parseInt(params[1]);
			publishProgress(0);
			Bitmap bitmap;
			String url = params[0];
			// bitmap = getBitMapFromSDCard(url);
			// if (bitmap != null) {
			// Log.d(TAG, "got bitmap from AppCache.");
			// publishProgress(100);
			// return bitmap;
			// }
			bitmap = getBitmapFromUrl(url);
			if (bitmap != null) {
				Log.d(TAG, "got bitmap from Remote: " + url);
				writeBitmap2AppCache(bitmap, url);
				publishProgress(100);
				return bitmap;
			}

			// Log.e(TAG, "failed to get image " + url
			// + ", Using default instead.");
			// bitmap = BitmapFactory.decodeResource(mContext.getResources(),
			// R.drawable.no_image);
			publishProgress(100);
			return bitmap;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			activity.mProgressIcon.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (result != null && loadingImgID == mCurrentImageIndex) {
				activity.setImageView(result);
				mCurrentBitmap = result;
			}
			if (loadingImgID == mCurrentImageIndex) {
				activity.mProgressIcon.setVisibility(View.GONE);
			}
			// activity.EnableNextPrevButtons(true);
		}
	}

	private Bitmap getBitmapFromUrl(String url) {

		Bitmap bitmap = null;
		URL u = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			u = new URL(url);
			conn = (HttpURLConnection) u.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			is = conn.getInputStream();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			bitmap = BitmapFactory.decodeStream(is, null, options);
			is.close();
		} catch (Exception e) {
			Log.e(TAG, "Failed to get image from " + url);
			e.printStackTrace();
		}
		return bitmap;
	}
	private Bitmap getBitmapFromSDCard(String url) {
		Bitmap bitmap = null;
		try {
			FileInputStream fis = new FileInputStream(getFile(url));
			bitmap = BitmapFactory.decodeStream(fis);
			fis.close();
		} catch (Exception e) {
			return bitmap;
		}
		return bitmap;
	}

	private void writeInputStream2AppCache(InputStream is, String url) {
		try {
			FileOutputStream fos = new FileOutputStream(getFile(url), false);
			int len = 0;
			byte[] b = new byte[is.available()];

			while ((len = is.read(b)) != -1) {
				fos.write(b, 0, len);
			}
			if (null != is) {
				is.close();
			}
			if (null != fos) {
				fos.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeBitmap2AppCache(Bitmap bitmap, String url) {
		if (bitmap == null)
			return;

		try {
			FileOutputStream fos = new FileOutputStream(getFile(url), false);
			byte[] bitmapByte = Bitmap2Byte(bitmap);
			ByteArrayInputStream bis = new ByteArrayInputStream(bitmapByte);
			int len = 0;
			byte[] b = new byte[bis.available()];
			while ((len = bis.read(b)) != -1) {
				fos.write(b, 0, len);
			}
			if (null != bis) {
				bis.close();
			}
			if (null != fos) {
				fos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getFile(String url) {
		File file = null;
		try {
			file = new File(getCachedFilename(url));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	private String getCachedFilename(String url) {
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
			path = mContext.getFilesDir().getPath() + File.separator
					+ APP_CACHE;
			file = new File(path);
			if (!file.exists())
				file.mkdir();

			if (!file.exists())
				return null;
		}

		return path + File.separator + url.hashCode();

		// return path + File.separator + getHostname(url) + "."
		// + getImageFilename(url);
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

	private byte[] Bitmap2Byte(Bitmap bm) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	private Bitmap Byte2Bimap(byte[] b) {
		if (b.length == 0) {
			return null;
		}
		return BitmapFactory.decodeByteArray(b, 0, b.length);
	}
}
