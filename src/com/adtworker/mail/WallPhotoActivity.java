package com.adtworker.mail;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.adtworker.mail.service.CallbackHandler;
import com.adtworker.mail.service.Service;
import com.adtworker.mail.service.entity.ImgInfo;
import com.adtworker.mail.service.entity.ImgLoadSupporter;
import com.adtworker.mail.view.SuperScrollView;

public class WallPhotoActivity extends Activity {
	private LinearLayout photoLayout1;
	private LinearLayout photoLayout2;
	private LinearLayout photoLayout3;
	public Service service;
	private SuperScrollView superScrollView;
	private final ArrayList<ImageView> imageViewList = new ArrayList<ImageView>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		service = Service.getInstance(this);
		setContentView(R.layout.wall_photo);
		superScrollView = (SuperScrollView) findViewById(R.id.photo_wall_scrollview);
		superScrollView.setVisibility(View.VISIBLE);
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

	private void addImageViewToList(ImageView imageView) {
		if (!imageViewList.contains(imageView)) {
			imageViewList.add(imageView);
		}
	}
	/**
	 * 图片墙初始化
	 */
	private void addGroupProducts() {
		wallPage++;
		initPhotoLayouts();
		try {
			if (photoWallWidth == 0) {
				photoWallWidth = getResources().getDisplayMetrics().widthPixels / 3;
			}
			List<ImgInfo> imgInfoList = BaiduImage.getImgInfoList(
					"android MM bizhi", 2, 20, 480, 800);
			for (int i = 0; i < imgInfoList.size(); i++) {

				ImageView imageView = generateWallImage(imgInfoList.get(i));
				addImageViewToList(imageView);
				updateImageOfProduct(imageView, false);
				addImageViewToLayout(imageView, photoWallWidth);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 将图片画到屏幕上
	 * 
	 * @param imageView
	 * @param photoWallWidth2
	 */
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

	/**
	 * 创建单个图片对象，此处的url暂时写死固定
	 * 
	 * @return
	 */
	private ImageView generateWallImage(ImgInfo imgInfo) {
		ImageView imageView = new ImageView(this);
		imageView.setImageDrawable(null);
		imageView.setBackgroundColor(Color.WHITE);
		imageView.setTag(imgInfo.getUrl());
		imageView.setClickable(true);
		imageView.setContentDescription(TAG_IMAGE_STATE_UNLOAD);
		return imageView;
	}

	private void updateImageOfProduct(ImageView imageView, boolean isLocal) {

		service.loadImg(getImgLoadSupporter(imageView));
	}

	private final String TAG_IMAGE_STATE_OK = "true";
	private final String TAG_IMAGE_STATE_LOADING = "isLoading";
	private final String TAG_IMAGE_STATE_UNLOAD = "unload";
	/**
	 * 定义图片异步处理加载
	 * 
	 * @param imageView
	 * @return
	 */
	private ImgLoadSupporter getImgLoadSupporter(final ImageView imageView) {
		CallbackHandler callbackHandler = new CallbackHandler(
				CallbackHandler.CallbackType.img) {
			@Override
			public void dispatchMessage(Message msg) {
				super.dispatchMessage(msg);
				Bitmap tempBitmap = (Bitmap) msg.obj;
				if (tempBitmap != null) {
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

	@Override
	protected void onDestroy() {
		if (service != null) {
			service.destroy();
		}
		super.onDestroy();
	}

}
