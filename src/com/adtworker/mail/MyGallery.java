/*
 * Copyright 2012 MITIAN Technology, Co., Ltd. All rights reserved.
 */
package com.adtworker.mail;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.adtworker.mail.constants.Constants;

/**
 * MyGallery.java
 * 
 * @author baojun
 */
public class MyGallery extends Activity {
	private ImageManager mImageManager;
	private HashMap<Integer, SoftReference<Bitmap>> mDataCache;
	private GridView mGridView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_gallery);
		mImageManager = ImageManager.getInstance(null);
		mDataCache = new HashMap<Integer, SoftReference<Bitmap>>();

		mGridView = (GridView) findViewById(R.id.GridView);
		mGridView.setAdapter(new ImageAdapter(this));
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				if (position != ImageManager.INVALID_PIC_INDEX) {
					setResult(position);
					finish();
				}
			}
		});

		int pos = mImageManager.getCurrent();
		if (pos != ImageManager.INVALID_PIC_INDEX) {
			mGridView.setSelection(pos);
		}
		setResult(pos);

		ViewGroup adLayout = (ViewGroup) findViewById(R.id.adLayout);
		Utils.setupAdLayout(this, adLayout, false);
	}

	@Override
	protected void onDestroy() {
		Log.d(Constants.TAG, "MyGallery onDestroy()");
		GridView gridView = (GridView) findViewById(R.id.GridView);
		int count = gridView.getCount();
		for (int i = 0; i < count; i++) {
			ImageView v = (ImageView) gridView.getChildAt(i);
			if (v != null) {
				if (v.getDrawable() != null)
					v.getDrawable().setCallback(null);
			}
		}
		for (int i = 0; i < mDataCache.size(); i++) {
			SoftReference<Bitmap> ref = mDataCache.get(i);
			if (ref != null && ref.get() != null) {
				Bitmap b = ref.get();
				b.recycle();
				b = null;
			}
		}
		mDataCache.clear();

		super.onDestroy();
	}

	private class ImageAdapter extends BaseAdapter {

		private final Context mContext;
		private final ImageManager mImageManager;

		public ImageAdapter(Context context) {
			mContext = context;
			mImageManager = ImageManager.getInstance(null);
		}

		@Override
		public int getCount() {
			return mImageManager.getImageListSize();
		}

		@Override
		public Drawable getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = null;
			if (convertView == null) {
				i = new ImageView(mContext);
			} else {
				i = (ImageView) convertView;
			}

			DisplayMetrics displayMetrics = mContext.getResources()
					.getDisplayMetrics();

			Bitmap bitmap = null;
			if (mDataCache.get(position) != null)
				bitmap = mDataCache.get(position).get();

			if (bitmap == null) {
				bitmap = mImageManager.getPosBitmap(position, true);
				mDataCache.put(position, new SoftReference<Bitmap>(bitmap));
			}
			i.setImageBitmap(bitmap);

			int width = displayMetrics.widthPixels / 3;
			int height = (int) (width / (float) bitmap.getWidth() * bitmap
					.getHeight());
			i.setLayoutParams(new AbsListView.LayoutParams(width, height));
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);

			return i;
		}
	}
}
