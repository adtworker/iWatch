package com.adtworker.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.util.Log;

import com.adtworker.mail.ImageManager.IMAGE_PATH_TYPE;
import com.adtworker.mail.constants.Constants;

public class DownloadManager {

	private static final String TAG = "iDownloadManager";
	private static DownloadManager mDownloadManager = null;
	private ExecutorService executorService;
	private Context mContext;
	private List<Integer> idList;

	public static DownloadManager getInstance() {
		if (null == mDownloadManager) {
			mDownloadManager = new DownloadManager(WatchApp.getInstance());
		}
		return mDownloadManager;
	}

	public DownloadManager(Context context) {
		mContext = context;
		idList = new ArrayList<Integer>();
	}

	public void addTask(DownloadItem item) {
		if (!idList.contains(item.getFileId())) {
			idList.add(item.getFileId());
			executorService.submit(new DownloadThread(item));
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
		private int id;
		private long length;
		private AdtImage image;

		public DownloadItem(int id, AdtImage adt) {
			this.id = id;
			this.image = adt;
			this.length = 0;
		}

		public int getFileId() {
			return id;
		}
		public long getFileLength() {
			return length;
		}
		public AdtImage getImage() {
			return image;
		}
	}

	class DownloadThread implements Runnable {
		private long finished;
		private long fileLength;
		private int fileId;
		private File downloadFile;
		private AdtImage image;

		public DownloadThread(DownloadItem item) {
			fileId = item.getFileId();
			finished = item.getFileLength();
			image = item.getImage();

			downloadFile = Utils.getFile(image.getFullUrl(), false);
			if (!downloadFile.exists()) {
				try {
					downloadFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				WatchApp.getImageManager().mImageList.get(fileId).byteLocal = downloadFile
						.length();
			}
		}

		@Override
		public void run() {
			AdtImage image = WatchApp.getImageManager().mImageList.get(fileId);
			Log.v(TAG, "file id=" + fileId + ", byteLocal=" + image.byteLocal);

			// // Create and initialize HTTP parameters
			// HttpParams params = new BasicHttpParams();
			// ConnManagerParams.setMaxTotalConnections(params, 100);
			// HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			//
			// // Create and initialize scheme registry
			// SchemeRegistry schemeRegistry = new SchemeRegistry();
			// schemeRegistry.register(new Scheme("http", PlainSocketFactory
			// .getSocketFactory(), 80));
			//
			// // Create an HttpClient with the ThreadSafeClientConnManager.
			// // This connection manager must be used if more than one thread
			// will
			// // be using the HttpClient.
			// ClientConnectionManager cm = new ThreadSafeClientConnManager(
			// params, schemeRegistry);
			//
			// HttpClient httpClient = new DefaultHttpClient(cm, params);

			HttpClient httpClient = new DefaultHttpClient();

			// set proxy if needed
			if (Proxy.getDefaultHost() != null) {
				Log.d(TAG, "using proxy: " + Proxy.getDefaultHost() + ":"
						+ Proxy.getDefaultPort());
				HttpHost proxy = new HttpHost(Proxy.getDefaultHost(),
						Proxy.getDefaultPort());
				httpClient.getParams().setParameter(
						ConnRoutePNames.DEFAULT_PROXY, proxy);
			}

			try {
				HttpGet httpGet = new HttpGet(image.getFullUrl());
				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();
				fileLength = httpEntity.getContentLength();
				if (WatchApp.getImageManager().getImagePathType() == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
					image.byteRemote = fileLength;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			Log.v(TAG, "byteLocal=" + image.byteLocal + ", byteRemote="
					+ image.byteRemote);
			if (image.byteLocal < image.byteRemote) {
				try {
					HttpGet httpGet = new HttpGet(image.getFullUrl());
					httpGet.addHeader("Range", "bytes=" + finished + "-"
							+ (fileLength - 1));
					HttpResponse httpResponse = httpClient.execute(httpGet);
					HttpEntity httpEntity = httpResponse.getEntity();
					FileOutputStream outputStream = new FileOutputStream(
							downloadFile, true);
					int count = 0;
					int deltaCount = 0;
					byte[] tmp = new byte[4 * 1024];
					InputStream inputStream = httpEntity.getContent();
					while (finished < fileLength) {
						if (DownloadManager.this.executorService.isShutdown()) {
							return;
						}
						count = inputStream.read(tmp);
						finished += count;
						deltaCount += count;

						// to support resuming from last break point
						if (finished > image.byteLocal) {
							outputStream.write(tmp, 0, count);
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
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			image.byteLocal = finished;
			Log.v(TAG, "byteLocal=" + image.byteLocal + ", byteRemote="
					+ image.byteRemote);
			httpClient.getConnectionManager().shutdown();
			Intent intent = new Intent(Constants.SET_PROGRESSBAR);
			intent.putExtra("fileId", fileId);
			intent.putExtra("progress2", 100);
			DownloadManager.this.mContext.sendBroadcast(intent);

			DownloadManager.this.idList.remove(fileId);

			httpClient.getConnectionManager().shutdown();
		}
	}
}
