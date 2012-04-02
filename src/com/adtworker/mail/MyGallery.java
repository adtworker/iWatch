/*
 * Copyright 2012 MITIAN Technology, Co., Ltd. All rights reserved.
 */
package com.adtworker.mail;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * MyGallery.java
 * 
 * @author baojun
 */
public class MyGallery extends Activity {
	private final static String TAG = "MyGallery";
	private ImageAdapter imageAdapter;

	private ArrayList<String> PhotoURLS = new ArrayList<String>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_gallery);

		imageAdapter = new ImageAdapter(this);
		final ImageView imgView = (ImageView) findViewById(R.id.GalleryView);
		Gallery g = (Gallery) findViewById(R.id.Gallery);
		g.setAdapter(imageAdapter);
		g.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				imgView.setImageDrawable(LoadImageFromURL(PhotoURLS
						.get(position)));
				imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			}
		});

		// replace this code to set your image urls in list
		PhotoURLS
				.add("http://drmcmm.baidu.com/media/id=PHn3PHm4rHc&gp=402&time=nHn1nHTsPWnvPs.jpg");
		PhotoURLS
				.add("http://pic5.nipic.com/20100104/2590249_091123039134_2.jpg");

		try {
			String keyword = Uri.encode("安卓手机 壁纸");
			PhotoURLS = (ArrayList<String>) BaiduImage.getImgUrl(keyword, 1,
					16, 480, 800);

		} catch (Exception e) {
			e.printStackTrace();
		}

		new AddImageTask().execute();

	}

	class AddImageTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			for (String url : PhotoURLS) {
				String filename = url.substring(url.lastIndexOf("/") + 1,
						url.length());
				// filename = "th_" + filename;
				String thumburl = url.substring(0, url.lastIndexOf("/") + 1);
				imageAdapter.addItem(LoadThumbnailFromURL(thumburl + filename));
				publishProgress();
				// SystemClock.sleep(200);
			}

			return (null);
		}

		@Override
		protected void onProgressUpdate(Void... unused) {
			imageAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Void unused) {
		}
	}

	private Drawable LoadThumbnailFromURL(String url) {
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
				Drawable d = Drawable.createFromStream(is, "src Name");
				return d;
			} else {
				Bitmap b = BitmapFactory.decodeResource(getResources(),
						R.drawable.no_image);
				Drawable d = new BitmapDrawable(b);
				return d;
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG)
					.show();
			Log.e(e.getClass().getName(), e.getMessage(), e);
			return null;
		}
	}

	private Drawable LoadImageFromURL(String url) {
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

				// Decode image size
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(is, null, o);

				// The new size we want to scale to
				final int REQUIRED_SIZE = 360;

				// Find the correct scale value. It should be the power of 2.
				int width_tmp = o.outWidth, height_tmp = o.outHeight;
				int scale = 1;
				Log.v(TAG, "starting crop:");
				while (true) {
					Log.v(TAG, "WxH = " + width_tmp + "x" + height_tmp
							+ ", scale=" + scale);

					if (width_tmp / 2 < REQUIRED_SIZE
							|| height_tmp / 2 < REQUIRED_SIZE) {
						break;
					}
					width_tmp /= 2;
					height_tmp /= 2;
					scale *= 2;
				}

				// Decode with inSampleSize
				is = bufferedHttpEntity.getContent();
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				Bitmap b = BitmapFactory.decodeStream(is, null, o2);
				Drawable d = new BitmapDrawable(b);
				return d;
			} else {
				Bitmap b = BitmapFactory.decodeResource(getResources(),
						R.drawable.no_image);
				Drawable d = new BitmapDrawable(b);
				return d;
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG)
					.show();
			Log.e(e.getClass().getName(), e.getMessage(), e);
			return null;
		}
	}
	public class ImageAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		private Context mContext;

		ArrayList<Drawable> drawablesFromUrl = new ArrayList<Drawable>();

		public ImageAdapter(Context c) {
			mContext = c;
			TypedArray a = obtainStyledAttributes(R.styleable.GalleryTheme);
			mGalleryItemBackground = a.getResourceId(
					R.styleable.GalleryTheme_android_galleryItemBackground, 0);
			a.recycle();
		}

		public void addItem(Drawable item) {
			drawablesFromUrl.add(item);
		}

		@Override
		public int getCount() {
			return drawablesFromUrl.size();
		}

		@Override
		public Drawable getItem(int position) {
			return drawablesFromUrl.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);

			i.setImageDrawable(drawablesFromUrl.get(position));
			i.setLayoutParams(new Gallery.LayoutParams(150, 240));
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			i.setBackgroundResource(mGalleryItemBackground);

			return i;
		}
	}
}
