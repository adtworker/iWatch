/**
 * File Name : CustomGallery.java
 * Project Name : HanzoApp 2.0
 * Package Name : com.iga.hanzo0
 * Created Time : 2012 2012-2-21 下午4:56:28
 * Created By : root
 */
package com.adtworker.mail.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * @author root & LuBin
 */
public class CustomGallery extends Gallery {

	/**
	 * @param context
	 * @param attrs
	 */
	public CustomGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		int kEvent;
		if (isScrollingLeft(e1, e2)) {
			// Check if scrolling left
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else {
			// Otherwise scrolling right
			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		onKeyDown(kEvent, null);
		return true;
	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}

}
