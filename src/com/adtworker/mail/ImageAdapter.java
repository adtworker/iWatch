package com.adtworker.mail;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
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
		mImageManager = ImageManager.getInstance();
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

	HashMap<Integer, SoftReference<ImageView>> mCached = new HashMap<Integer, SoftReference<ImageView>>();

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		SoftReference<ImageView> imgReference = mCached.get(position);
		if (imgReference != null && imgReference.get() != null) {
			return imgReference.get();
		}

		ImageView i = null;
		if (convertView == null) {
			i = new ImageView(mContext);
		} else {
			i = (ImageView) convertView;
		}
		Bitmap bitmap = mImageManager.getPosBitmap(position, true);
		DisplayMetrics displayMetrics = mContext.getResources()
				.getDisplayMetrics();
		int width = displayMetrics.widthPixels * 5 / 12;
		int height = displayMetrics.heightPixels * 5 / 12;
		if (bitmap != null) {
			height = (int) (width / (float) bitmap.getWidth() * bitmap
					.getHeight());
		}
		i.setImageBitmap(bitmap);
		i.setLayoutParams(new CoverFlow.LayoutParams(width, height));
		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		// i.setBackgroundResource(mGalleryItemBackground);

		mCached.put(position, new SoftReference<ImageView>(i));
		return i;
	}
}