package com.adtworker.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adtworker.mail.constants.Constants;
import com.adtworker.mail.util.HttpUtils;

public class DownloadManager {

	private static final String TAG = "iDownloadManager";
	private static DownloadManager mDownloadManager = null;
	private ExecutorService executorService;
	private final Context mContext;
	private final List<Integer> idList;
	private final List<Integer> idTbList;

	public static DownloadManager getInstance() {
		if (null == mDownloadManager) {
			mDownloadManager = new DownloadManager(WatchApp.getInstance());
		}
		return mDownloadManager;
	}

	public void recycle() {
		if (mDownloadManager != null) {
			idList.clear();
			idTbList.clear();

			if (!mDownloadManager.isStop())
				mDownloadManager.stop();

			mDownloadManager = null;
		}
	}

	public DownloadManager(Context context) {
		mContext = context;
		idList = new ArrayList<Integer>();
		idTbList = new ArrayList<Integer>();
	}

	public String getDownloadsInfo() {
		String string = "";
		if (!idTbList.isEmpty())
			string += "Thumbnails: " + idTbList.toString() + "\n";
		for (int i = 0; i < idList.size(); i++) {
			int j = idList.get(i);
			AdtImage img = WatchApp.getImageManager().mImageList.get(j);
			String hashCode = Integer.toHexString(img.getFullUrl().hashCode());
			string += String.format("[%04d] %07d / %07d %s\n", j,
					img.byteLocal, img.byteRemote, hashCode);
		}
		return string;
	}

	public void addTask(DownloadItem item) {
		if (item == null || item.image == null) {
			Log.e(TAG, "failed to add null task.");
			return;
		}

		Log.d(TAG, "addTask: " + item.getFileId() + ") " + item.getUrl());

		if (item.isThumb()) {
			if (!idTbList.contains(item.getFileId())) {
				idTbList.add(item.getFileId());
				executorService.submit(new DownloadThread(item));
			}
		} else {
			if (!idList.contains(item.getFileId())) {
				idList.add(item.getFileId());
				executorService.submit(new DownloadThread(item));
			}
		}
	}

	public void stop() {
		executorService.shutdown();
	}

	public void start() {
		executorService = Executors.newFixedThreadPool(5);
	}

	public boolean isStop() {
		return executorService.isShutdown();
	}

	public static class DownloadItem {
		private int fileid;
		private long length;
		private AdtImage image = null;
		private String url;
		private boolean bThumb = false;

		public DownloadItem(String szUrl, boolean isThumb) {
			url = szUrl;
			bThumb = isThumb;
			length = 0;

			for (int i = 0; i < WatchApp.getImageManager().getImageListSize(); i++) {
				AdtImage img = WatchApp.getImageManager().mImageList.get(i);
				String urlTmp = bThumb ? img.getTbnUrl() : img.getFullUrl();
				if (urlTmp.equals(url)) {
					Log.d(TAG, "new DownloadItem, id=" + i + ", url=" + url);
					fileid = i;
					image = img;
					return;
				}
			}
			Log.e(TAG, "Can't find url " + url);

		}

		public DownloadItem(int id, AdtImage adt) {
			fileid = id;
			image = adt;
			length = 0;
			bThumb = false;
		}

		public int getFileId() {
			return fileid;
		}

		public AdtImage getImage() {
			return image;
		}

		public long getFileLength() {
			if (!bThumb) {
				File file = Utils.getTempFile(image.getFullUrl(), bThumb);
				if (file.exists()) {
					length = file.length();
				}
			}

			return length;
		}

		public boolean isThumb() {
			return bThumb;
		}

		public String getUrl() {
			return url;
		}
	}

	class DownloadThread implements Runnable {
		private long finished;
		private long fileLength;
		private final int fileId;
		private File downloadFile;
		private final AdtImage image;
		private final boolean bThumb;

		public DownloadThread(DownloadItem item) {
			fileId = item.getFileId();
			finished = item.getFileLength();
			image = item.getImage();
			bThumb = item.isThumb();

			image.byteLocal = finished;
		}

		@Override
		public void run() {

			HttpClient httpClient = HttpUtils.getHttpClient();
			String url = image.getFullUrl();

			if (bThumb) {

				url = image.getTbnUrl();
				downloadFile = Utils.getFile(url, bThumb);
				try {
					HttpGet httpGet = new HttpGet(url);
					HttpResponse httpResponse = httpClient.execute(httpGet);
					HttpEntity httpEntity = httpResponse.getEntity();
					fileLength = httpEntity.getContentLength();
					InputStream inputStream = httpEntity.getContent();
					FileOutputStream outputStream = new FileOutputStream(
							downloadFile, false);
					finished = 0;
					byte[] b = new byte[8 * 1024];
					while (finished < fileLength) {
						int len = inputStream.read(b, 0, b.length);
						outputStream.write(b, 0, len);
						finished += len;
					}
					inputStream.close();
					outputStream.close();
					Log.d(TAG, fileId + ") downloaded thumbnail fileLength="
							+ fileLength + ", finished=" + finished);

					Intent intent = new Intent(Constants.SET_PROGRESSBAR);
					intent.putExtra("fileId", fileId);
					intent.putExtra("progress3", 100);
					DownloadManager.this.mContext.sendBroadcast(intent);

				} catch (Exception e) {
					e.printStackTrace();
				}

				idTbList.remove(idTbList.indexOf(fileId));
				httpClient.getConnectionManager().shutdown();
				return;

			} else {
				downloadFile = Utils.getTempFile(url, bThumb);
			}

			Log.v(TAG, fileId + ") " + url + ", byteLocal=" + image.byteLocal);

			try {
				HttpGet httpGet = new HttpGet(url);
				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();
				fileLength = httpEntity.getContentLength();
				image.byteRemote = fileLength;

			} catch (Exception e) {
				e.printStackTrace();
			}

			Log.v(TAG, fileId + ") byteLocal=" + image.byteLocal
					+ ", byteRemote=" + image.byteRemote);

			if (image.byteLocal < image.byteRemote && image.byteRemote > 1000) {
				try {
					HttpGet httpGet = new HttpGet(url);
					httpGet.addHeader("Range", "bytes=" + finished + "-"
							+ (fileLength - 1));
					HttpResponse httpResponse = httpClient.execute(httpGet);
					HttpEntity httpEntity = httpResponse.getEntity();
					FileOutputStream outputStream = new FileOutputStream(
							downloadFile, true);
					int count = 0;
					int deltaCount = 0;
					byte[] tmp = new byte[8 * 1024];
					InputStream inputStream = httpEntity.getContent();
					while (finished < fileLength) {
						if (DownloadManager.this.executorService.isShutdown()) {
							Log.d(TAG, "Service is down, return.");
							return;
						}
						count = inputStream.read(tmp, 0, tmp.length);
						finished += count;
						deltaCount += count;

						// to support resuming from last break point
						if (finished > image.byteLocal) {
							outputStream.write(tmp, 0, count);
							image.byteLocal = downloadFile.length();
						}

						if (deltaCount / (float) fileLength > 0.02) {
							deltaCount = 0;
							int progress = (int) (finished * 100 / fileLength);
							if (progress != 100) {
								Intent intent = new Intent(
										Constants.SET_PROGRESSBAR);
								intent.putExtra("fileId", fileId);
								intent.putExtra("progress2", progress);
								DownloadManager.this.mContext
										.sendBroadcast(intent);
							}
						}
					}
					outputStream.close();
					Utils.renameTempFile(url, bThumb);

				} catch (Exception e) {
					Log.e(TAG, "download exception.");
					e.printStackTrace();
				}

				Intent intent = new Intent(Constants.SET_PROGRESSBAR);
				intent.putExtra("fileId", fileId);
				intent.putExtra("progress2", 100);
				DownloadManager.this.mContext.sendBroadcast(intent);

				Log.v(TAG, fileId + ") byteLocal=" + image.byteLocal
						+ ", byteRemote=" + image.byteRemote);

				idList.remove(idList.indexOf(fileId));
				httpClient.getConnectionManager().shutdown();
			}
		}
	}
}
