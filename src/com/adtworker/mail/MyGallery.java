/*
 * Copyright 2012 MITIAN Technology, Co., Ltd. All rights reserved.
 */
package com.adtworker.mail;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * MyGallery.java
 * 
 * @author baojun
 */
public class MyGallery extends Activity {
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
		final Gallery g = (Gallery) findViewById(R.id.Gallery);
		g.setAdapter(imageAdapter);

		g.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				imgView.setImageBitmap(mImageManager.getPosBitmap(position,
						false));
				imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				setResult(position);
			}
		});

		imgView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int gv = g.getVisibility();
				g.setVisibility(gv == View.GONE ? View.VISIBLE : View.GONE);
			}

		});

		int pos = mImageManager.getCurrent();
		if (pos != ImageManager.INVALID_PIC_INDEX) {
			g.setSelection(pos);
		}
		setResult(pos);

		ViewGroup adLayout = (ViewGroup) findViewById(R.id.adLayout);
		Utils.setupAdLayout(this, adLayout, false);
	}
}
