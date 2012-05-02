package com.adtworker.mail;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	int mGalleryItemBackground;
	private final Context mContext;
	private final ImageManager mImageManager;

	public ImageAdapter(Context context) {
		mContext = context;
		mImageManager = ImageManager.getInstance(null);
		TypedArray a = mContext
				.obtainStyledAttributes(R.styleable.GalleryTheme);
		mGalleryItemBackground = a.getResourceId(
				R.styleable.GalleryTheme_android_galleryItemBackground, 0);
		a.recycle();
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
		ImageView i = new ImageView(mContext);
		DisplayMetrics displayMetrics = mContext.getResources()
				.getDisplayMetrics();
		int width = displayMetrics.widthPixels / 4;
		int height = displayMetrics.heightPixels / 4;

		i.setImageBitmap(mImageManager.getPosBitmap(position, true));
		i.setLayoutParams(new CoverFlow.LayoutParams(width, height));
		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		// i.setBackgroundResource(mGalleryItemBackground);

		return i;
	}
}