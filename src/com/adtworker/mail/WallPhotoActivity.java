package com.adtworker.mail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.adtworker.mail.service.CallbackHandler;
import com.adtworker.mail.service.entity.ImgLoadSupporter;

public class WallPhotoActivity extends Activity {
	private LinearLayout photoLayout1;
	private LinearLayout photoLayout2;
	private LinearLayout photoLayout3;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wall_photo);
		addGroupProducts();
	}
	private void initPhotoLayouts() {
		if (photoLayout1 == null) {
			photoLayout1 = (LinearLayout) findViewById(R.id.photo_layout1);
			photoLayout2 = (LinearLayout) findViewById(R.id.photo_layout2);
			photoLayout3 = (LinearLayout) findViewById(R.id.photo_layout3);
			photoLayout1.setTag(0);
			photoLayout2.setTag(0);
			photoLayout3.setTag(0);
		}
	}
	private int photoWallWidth = 0;
	private int wallPage = 0;
	private void addGroupProducts() {
		initPhotoLayouts();
		try {
			if (photoWallWidth == 0) {
				photoWallWidth = getResources().getDisplayMetrics().widthPixels / 3;
			}
			for (int i = 0; i < 10; i++) {
				wallPage++;
				ImageView imageView = generateWallImage();
				addImageViewToLayout(imageView, photoWallWidth);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void addImageViewToLayout(ImageView imageView, int photoWallWidth2) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				photoWallWidth, (photoWallWidth * 320) / 240);
		params.setMargins(0, 0, 0, (int) (4 * getResources()
				.getDisplayMetrics().density));
		LinearLayout tempLayout = getLayout();
		tempLayout.addView(imageView, params);

		int length = (Integer) tempLayout.getTag();
		tempLayout.setTag(length + ((photoWallWidth * 320) / 240));
	}
	private LinearLayout getLayout() {
		int length1 = (Integer) photoLayout1.getTag();
		int length2 = (Integer) photoLayout2.getTag();
		int length3 = (Integer) photoLayout3.getTag();
		if ((length1 <= length2) && (length1 <= length3)) {
			return photoLayout1;
		} else {
			if (length2 <= length3) {
				return photoLayout2;
			} else {
				return photoLayout3;
			}
		}
	}

	private ImageView generateWallImage() {
		ImageView imageView = new ImageView(this);
		imageView.setImageDrawable(null);
		imageView.setBackgroundColor(Color.WHITE);
		imageView
				.setTag("http://tupian.feiku.com/data/pic/20100119/1263864880739435.jpg");
		imageView.setClickable(true);
		return imageView;
	}

	private void updateImageOfProduct(ImageView imageView, boolean isLocal) {
		String contentString = imageView.getContentDescription().toString();
		if (contentString.equals(TAG_IMAGE_STATE_OK)
				|| contentString.equals(TAG_IMAGE_STATE_LOADING)) {
			return;
		}
		getImgLoadSupporter(imageView);
	}

	private final String TAG_IMAGE_STATE_OK = "true";
	private final String TAG_IMAGE_STATE_LOADING = "isLoading";
	private final String TAG_IMAGE_STATE_UNLOAD = "unload";
	private ImgLoadSupporter getImgLoadSupporter(final ImageView imageView) {
		CallbackHandler callbackHandler = new CallbackHandler(
				CallbackHandler.CallbackType.img) {
			@Override
			public void dispatchMessage(Message msg) {
				super.dispatchMessage(msg);
				Bitmap tempBitmap = (Bitmap) msg.obj;
				String contentString = imageView.getContentDescription()
						.toString();
				if (tempBitmap != null
						&& !contentString.equals(TAG_IMAGE_STATE_UNLOAD)) {
					imageView.setImageBitmap(tempBitmap);
					imageView.setContentDescription(TAG_IMAGE_STATE_OK);
					tempBitmap = null;
				}
				msg.obj = null;
				// tempBitmap.recycle();
			}
		};
		ImgLoadSupporter imgLoadSupporter = new ImgLoadSupporter();
		imgLoadSupporter.callbackHandler = callbackHandler;
		imgLoadSupporter.url = (String) imageView.getTag();
		imageView.setContentDescription(TAG_IMAGE_STATE_LOADING);
		return imgLoadSupporter;
	}

}
