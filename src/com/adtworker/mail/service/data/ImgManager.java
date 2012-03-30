package com.adtworker.mail.service.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

	public InputStream getInputStream(String url) throws Exception {
		InputStream is = null;
		is = getInputStreamFromSDCard(url);

		if (is == null) {
			is = getInputStreamFromUrl(url);
			// writeInputStream2SDCard(is, url);
		}
		return is;
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

	private InputStream getInputStreamFromUrl(String url) {
		URL u = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			u = new URL(url);
			conn = (HttpURLConnection) u.openConnection();
			is = conn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}

	private Bitmap getBitMapFromSDCard(String url) throws Exception {
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

	private InputStream getInputStreamFromSDCard(String url) throws Exception {
		InputStream is = null;
		try {
			FileInputStream fis = new FileInputStream(getFile(url));
			is = fis;
		} catch (Exception e) {
			return is;
		}
		return is;
	}

	private void writeImg2SDCard(Bitmap bitmap, String url) throws Exception {
		FileOutputStream fos = new FileOutputStream(getFile(url), false);
		byte[] bitmapByte = UtilImgTrans.Bitmap2Byte(bitmap);
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
	}

	private void writeInputStream2SDCard(InputStream is, String url)
			throws Exception {
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
	}

	private File getFile(String url) {
		File file = null;
		try {
			file = new File(PATH + "/" + UtilMD5.getMD5String(url));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}
}
