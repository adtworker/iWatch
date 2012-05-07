package com.adtworker.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.adtworker.mail.DownloadManager.DownloadItem;
import com.adtworker.mail.constants.Constants;

public class ImageManager {

	public enum IMAGE_PATH_TYPE {
		LOCAL_ASSETS, // assets/
		PHONE_STORAGE, // /mnt/sdcard
		EXTERNAL_SD_CARD, // /mnt/sdcard-ext
		PACKAGE_PRIVATE, // /data/data/$package_name
		REMOTE_HTTP_URL, // http://xxx
		IMAGE_PATH_TYPE_LEN
	}

	private final static String TAG = "ImageManager";
	private final String IMAGE_SUBFOLDER_IN_ASSETS = "pics";
	private final Context mContext;
	private boolean mInitListFailed = false;
	private boolean mInitInProcess = false;
	private int mCurrentImageIndex = INVALID_PIC_INDEX;
	private final int[] mCurrentIndexArray = new int[IMAGE_PATH_TYPE.IMAGE_PATH_TYPE_LEN
			.ordinal()];
	private final int mSearchPageNum = 3;
	private final int mSearchPageSize = 80;
	private int mSearchImageWidth = 960;
	private int mSearchImageHeight = 800;
	private Bitmap mCurrentBitmap = null;

	public final static int INVALID_PIC_INDEX = -1;

	public IMAGE_PATH_TYPE mImagePathType;
	private IMAGE_PATH_TYPE mImagePathTypeLast;
	private Map<IMAGE_PATH_TYPE, ArrayList<AdtImage>> mImageListMap = new HashMap<IMAGE_PATH_TYPE, ArrayList<AdtImage>>();
	public ArrayList<AdtImage> mImageList = null;
	private final ArrayList<String> mQueryKeywords = new ArrayList<String>();

	private DownloadManager mDownloadManager = null;

	private static ImageManager mImageManager = null;
	public static ImageManager getInstance() {
		if (null == mImageManager) {
			Log.d(TAG, "Initizing an ImageManager.");
			mImageManager = new ImageManager();
		}
		return mImageManager;
	}

	public void recycle() {
		if (mImageManager != null) {
			for (int i = 0; i < mImageListMap.size(); i++) {
				if (mImageListMap.get(i) != null)
					mImageListMap.get(i).clear();
			}
			mImageListMap.clear();
			mImageManager = null;

			if (mDownloadManager != null) {
				if (!mDownloadManager.isStop())
					mDownloadManager.stop();
				mDownloadManager = null;
			}
		}
	}

	public ImageManager() {
		mContext = WatchApp.getInstance();
		mDownloadManager = DownloadManager.getInstance();
		mDownloadManager.start();

		for (int i = 0; i < mCurrentIndexArray.length; i++)
			mCurrentIndexArray[i] = INVALID_PIC_INDEX;
		mCurrentImageIndex = INVALID_PIC_INDEX;

		mImagePathType = IMAGE_PATH_TYPE.LOCAL_ASSETS;
		setImagePathType(mImagePathType);
	}

	public void setImagePathType(IMAGE_PATH_TYPE type) {
		mCurrentIndexArray[mImagePathType.ordinal()] = mCurrentImageIndex;
		mImagePathTypeLast = mImagePathType;
		mImagePathType = type;
		initImageList();
	}

	public IMAGE_PATH_TYPE getImagePathType() {
		return mImagePathType;
	}

	public boolean isInitListFailed() {
		return mInitListFailed;
	}

	public boolean isInitInProcess() {
		return mInitInProcess;
	}

	public void setQueryKeyword(String args) {
		mQueryKeywords.clear();
		String[] words = args.split(" ");
		for (String word : words) {
			mQueryKeywords.add(word);
		}
	}

	public void setQueryImgSize(int width, int height) {
		mSearchImageWidth = width;
		mSearchImageHeight = height;
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
		if (!isValidIndex(index)) {
			int size = getImageListSize();
			index = index + size % size;
		}

		mCurrentImageIndex = index;
		if (mCurrentBitmap != null) {
			mCurrentBitmap = null;
		}
	}

	public boolean isCurrentAsset() {
		return mImageList.get(mCurrentImageIndex).isAsset;
	}

	public String getCurrentStr() {
		return mImageList.get(mCurrentImageIndex).getFullUrl();
	}

	public String getCurrentStrTb() {
		return mImageList.get(mCurrentImageIndex).getTbnUrl();
	}

	public String getCurrentStrLocal() {
		return Utils.getCachedFilename(mImageList.get(mCurrentImageIndex)
				.getFullUrl(), false);
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

	private Bitmap getBitmap(String strImage) {
		Bitmap bitmap = null;
		try {
			if (mImagePathType == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
				InputStream is = mContext.getAssets().open(strImage);
				bitmap = BitmapFactory.decodeStream(is);
			} else if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
				bitmap = getBitmapFromSDCard(strImage, false);

				if (bitmap == null) {
					String tbUrl = getCurrentStrTb();
					Bitmap bm = getBitmapFromSDCard(tbUrl, true);
					if (bm == null) {
						bm = getBitmapFromUrl(tbUrl);
					}

					DownloadItem item = new DownloadItem(mCurrentImageIndex,
							new AdtImage(strImage));
					mDownloadManager.addTask(item);

					bitmap = bm;
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

	public Bitmap getPosBitmap(int pos, boolean tbFirst) {
		if (pos < 0 || pos >= getImageListSize())
			return null;

		AdtImage adt = mImageList.get(pos);
		String url = adt.getFullUrl();
		if (tbFirst && adt.hasThumb) {
			url = adt.getTbnUrl();
		}
		Bitmap bitmap = null;
		try {
			if (mImagePathType == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
				InputStream is = mContext.getAssets().open(url);
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inPurgeable = true;
				opt.inTempStorage = new byte[16 * 1000];
				if (tbFirst)
					opt.inSampleSize = 4;
				else
					opt.inSampleSize = 1;
				bitmap = BitmapFactory.decodeStream(is, null, opt);
				is.close();
				is = null;
			} else if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
				bitmap = getBitmapFromSDCard(url, false);
				if (bitmap == null) {
					bitmap = getBitmapFromUrl(url);
				}
				if (tbFirst && adt.hasThumb && bitmap == null) {
					url = adt.getFullUrl();
					bitmap = getBitmapFromSDCard(url, 4, false);
				}
				if (!tbFirst && bitmap == null) {
					bitmap = getBitmapFromUrl(url);
					if (bitmap != null)
						writeBitmap2AppCache(bitmap, url, false);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	private void initImageList() {
		if (mImageListMap.get(mImagePathType) != null) {
			mImageList = mImageListMap.get(mImagePathType);
			mCurrentImageIndex = mCurrentIndexArray[mImagePathType.ordinal()];
			return;
		}

		switch (mImagePathType) {
			case LOCAL_ASSETS :
				mInitInProcess = true;
				mInitListFailed = true;
				ArrayList<AdtImage> tempImageList = new ArrayList<AdtImage>();
				ArrayList<String> arrayList = getAssetsImagesList(IMAGE_SUBFOLDER_IN_ASSETS);
				for (int i = 0; i < arrayList.size(); i++) {
					AdtImage image = new AdtImage(arrayList.get(i), true);
					tempImageList.add(image);
				}
				arrayList.clear();
				if (tempImageList.size() != 0) {
					mImageListMap.put(IMAGE_PATH_TYPE.LOCAL_ASSETS,
							tempImageList);
					mImageList = mImageListMap
							.get(IMAGE_PATH_TYPE.LOCAL_ASSETS);
					mCurrentImageIndex = mCurrentIndexArray[mImagePathType
							.ordinal()];
					mInitListFailed = false;
					mInitInProcess = false;
				} else {
					mImagePathType = mImagePathTypeLast;
				}
				break;

			case REMOTE_HTTP_URL :
				new getImagesTask().execute();
				break;
		}
	}
	private class getImagesTask
			extends
				AsyncTask<Void, Void, ArrayList<AdtImage>> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			Intent intent = new Intent(Constants.SET_BUTTONSTATE);
			intent.putExtra("buttonState", false);
			mContext.sendBroadcast(intent);

			mInitListFailed = true;
			mInitInProcess = true;
		}

		@Override
		protected ArrayList<AdtImage> doInBackground(Void... params) {
			ArrayList<AdtImage> tempImageList = new ArrayList<AdtImage>();
			try {
				for (int k = 0; k < mQueryKeywords.size(); k++) {
					Log.d(TAG, "querying word " + mQueryKeywords.get(k));
					String keyword = URLEncoder.encode(mQueryKeywords.get(k),
							"GBK");

					for (int i = 1; i <= mSearchPageNum; i++) {
						List<AdtImage> temp = ImageSearchAdapter.getImgList(
								keyword, mSearchImageWidth, mSearchImageHeight,
								i, tempImageList.size());

						for (int j = 0; j < temp.size(); j++) {
							AdtImage img = temp.get(j);
							if (img.getFullUrl().toLowerCase().endsWith(".gif"))
								continue;

							tempImageList.add(img);
						}
					}
				}
				Log.d(TAG, "image list size = " + tempImageList.size());

				if (tempImageList.size() != 0) {
					mInitListFailed = false;
				}

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			return tempImageList;
		}

		@Override
		protected void onPostExecute(ArrayList<AdtImage> result) {
			super.onPostExecute(result);
			mInitInProcess = false;

			Intent intent = new Intent(Constants.SET_BUTTONSTATE);
			intent.putExtra("buttonState", true);
			mContext.sendBroadcast(intent);

			if (!mInitListFailed) {
				mImageListMap.put(IMAGE_PATH_TYPE.REMOTE_HTTP_URL, result);
				mImageList = mImageListMap.get(mImagePathType);
				new loadAllImageTask().execute();
			} else {
				mImagePathType = mImagePathTypeLast;
			}
			mCurrentImageIndex = mCurrentIndexArray[mImagePathType.ordinal()];
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
					url = img.getTbnUrl();
				} else {
					url = img.getFullUrl();
				}

				Bitmap bitmap = getBitmapFromSDCard(url, img.hasThumb);
				if (bitmap == null) {
					bitmap = getBitmapFromUrl(url);
					if (bitmap != null) {
						writeBitmap2AppCache(bitmap, url, img.hasThumb);
					} else {
						Log.e(TAG, "thumbnail image " + i + " is null.");
					}
				}

				// int progress = (i + 1) * 100 / mImageList.size();
				// Intent intent = new Intent(Constants.SET_PROGRESSBAR);
				// intent.putExtra("progress", progress);
				// WatchApp.getInstance().sendBroadcast(intent);

			}
			return null;
		}
	}

	public Bitmap getBitmapFromUrl(String url) {
		return getBitmapFromUrl(url, false);
	}

	public Bitmap getBitmapFromUrl(String url, boolean background) {

		if (background) {
			DownloadItem item = new DownloadItem(mCurrentImageIndex,
					new AdtImage(url));
			mDownloadManager.addTask(item);
			return null;
		}

		Bitmap bitmap = null;
		URL u = null;
		HttpURLConnection conn = null;
		InputStream is = null;

		try {
			u = new URL(url);
		} catch (MalformedURLException e2) {
			Log.e(TAG, "incorrect url " + url);
			e2.printStackTrace();
			return null;
		}
		try {
			String proxyHost = android.net.Proxy.getDefaultHost();
			if (proxyHost != null) {
				java.net.Proxy p = new java.net.Proxy(java.net.Proxy.Type.HTTP,
						new InetSocketAddress(
								android.net.Proxy.getDefaultHost(),
								android.net.Proxy.getDefaultPort()));
				conn = (HttpURLConnection) u.openConnection(p);
			} else {
				conn = (HttpURLConnection) u.openConnection();
			}
		} catch (IOException e1) {
			Log.e(TAG, "IOException on connecting " + url);
			e1.printStackTrace();
			return null;
		}
		conn.setConnectTimeout(5000);
		try {
			is = conn.getInputStream();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			bitmap = BitmapFactory.decodeStream(is, null, options);
			is.close();
			is = null;
		} catch (IOException e) {
			Log.e(TAG, "IOException on loading " + url);
			e.printStackTrace();
			conn.disconnect();
			return null;
		}
		conn.disconnect();
		return bitmap;
	}

	public Bitmap getBitmapFromSDCard(String url, boolean isThumb) {
		Bitmap bitmap = null;
		try {
			FileInputStream fis = new FileInputStream(Utils.getFile(url,
					isThumb));
			bitmap = BitmapFactory.decodeStream(fis);
			fis.close();
			fis = null;
		} catch (Exception e) {
			return bitmap;
		}
		return bitmap;
	}

	public Bitmap getBitmapFromSDCard(String url, int inSampleSize,
			boolean isThumb) {
		Bitmap bitmap = null;
		try {
			FileInputStream fis = new FileInputStream(Utils.getFile(url,
					isThumb));
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inSampleSize = inSampleSize;
			bitmap = BitmapFactory.decodeStream(fis, null, opt);
			fis.close();
			fis = null;
		} catch (Exception e) {
			return bitmap;
		}
		return bitmap;
	}

	private void writeInputStream2AppCache(InputStream is, String url,
			boolean isThumb) {
		try {
			FileOutputStream fos = new FileOutputStream(Utils.getFile(url,
					isThumb), false);
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

	private void writeBitmap2AppCache(Bitmap bitmap, String url, boolean isThumb) {
		if (bitmap == null)
			return;

		try {
			FileOutputStream fos = new FileOutputStream(Utils.getFile(url,
					isThumb), false);
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
		bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
		return baos.toByteArray();
	}

	private Bitmap Byte2Bitmap(byte[] b) {
		if (b.length == 0) {
			return null;
		}
		return BitmapFactory.decodeByteArray(b, 0, b.length);
	}
}
