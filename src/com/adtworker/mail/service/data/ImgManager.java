package com.adtworker.mail.service.data;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class ImgManager {

	private static final String PATH = Environment
			.getExternalStorageDirectory() + "/.airAD/AppCache";
	private final int zoomSize = 1;

	public ImgManager() {
		File rootFile = new File(PATH);
		if (!rootFile.exists()) {
			rootFile.mkdirs();
		}
	}

	public Bitmap getImg(String url) throws Exception {
		// if (url.endsWith("160x160.jpg")) {
		// url = url.replace("160x160", "120x120");
		// }
		Bitmap bitmap = null;
		// bitmap = getBitMapFromSDCard(url);
		if (bitmap == null) {
			bitmap = getBitMapFromUrl(url);
		}
		return bitmap;
	}

	private Bitmap getBitMapFromUrl(String url) {

		Bitmap bitmap = null;
		URL u = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			u = new URL(url);
			conn = (HttpURLConnection) u.openConnection();
			conn.setConnectTimeout(5 * 1000);
			is = conn.getInputStream();
			BitmapFactory.Options options = new BitmapFactory.Options();
			if (!url.endsWith("160.jpg")) {
				options.inSampleSize = zoomSize + 1;
			}
			options.inSampleSize = zoomSize;
			bitmap = BitmapFactory.decodeStream(is, null, options);
			// writeImg2SDCard(bitmap, url);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

}
