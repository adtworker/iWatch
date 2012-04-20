/*
 * Copyright 2012 MITIAN Technology, Co., Ltd. All rights reserved.
 */
package com.adtworker.mail;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * MyGallery.java
 * 
 * @author baojun
 */
public class MyGallery extends Activity {
	private final static String TAG = "MyGallery";
	private ImageAdapter imageAdapter;
	private ImageManager mImageManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_gallery);
		mImageManager = ImageManager.getInstance(null);

		imageAdapter = new ImageAdapter(this);
		final ImageView imgView = (ImageView) findViewById(R.id.GalleryView);
		Gallery g = (Gallery) findViewById(R.id.Gallery);
		g.setAdapter(imageAdapter);
		g.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				mImageManager.setCurrent(position);
				imgView.setImageBitmap(mImageManager.getPosBitmap(position,
						false));
				imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			}
		});

		new AddImageTask().execute();

	}

	class AddImageTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... unused) {

			for (int i = 0; i < mImageManager.getImageListSize(); i++) {
				Drawable d = new BitmapDrawable(mImageManager.getPosBitmap(i,
						true));
				imageAdapter.addItem(d);
				publishProgress();
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

	public class ImageAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		private final Context mContext;

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
			i.setLayoutParams(new CoverFlow.LayoutParams(300, 300));
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			i.setBackgroundResource(mGalleryItemBackground);

			return i;
		}
	}
}
