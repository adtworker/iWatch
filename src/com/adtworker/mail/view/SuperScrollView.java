/**
 * File Name : SuperScrollView.java
 * Project Name : PhotoWall
 * Package Name : com.test.photowall
 * Created Time : 2012 2012-2-17 下午3:19:14
 * Created By : root
 */
package com.adtworker.mail.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class SuperScrollView extends ScrollView {

	private float flingProportion = 1;
	private final float FLING_DEFAULE_PROPORTION = 0.6f;
	private final int LIMIT_Y = 10;
	private ScrollViewListener scrollViewListener = null;

	public SuperScrollView(Context context) {
		super(context);
	}

	public SuperScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SuperScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public int computeVerticalScrollRange() {
		return super.computeHorizontalScrollRange();
	}

	@Override
	public int computeVerticalScrollOffset() {
		return super.computeVerticalScrollOffset();
	}

	public void setScrollViewListener(ScrollViewListener scrollViewListener) {
		this.scrollViewListener = scrollViewListener;
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);
		int newy = y;
		if (newy - oldy > LIMIT_Y) {
			newy = LIMIT_Y;
			flingProportion = FLING_DEFAULE_PROPORTION;
		} else if (newy - oldy < -LIMIT_Y) {
			newy = -LIMIT_Y;
			flingProportion = FLING_DEFAULE_PROPORTION;
		}
		if (scrollViewListener != null) {
			scrollViewListener.onScrollChanged(this, x, newy, oldx, oldy);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		return true;
	}

	@Override
	public void fling(int velocityY) {
		Log.e("fling===========", velocityY + "");
		// super.fling((int) (velocityY * flingProportion));
		super.fling((int) (velocityY * 0.8));
		flingProportion = 1;
	}

}
