/**
 * =====================================================================
 *
 * @file  CoverflowEx.java
 * @Module Name   com.lenovo.leos.commonwidgets
 * @author yueliang@use.com.cn
 * @OS version  LeOS2.0
 * @Product type: Lepad
 * @date   2010-11-17
 * @brief  This file is the http **** implementation.
 * @This file is responsible by HomePane TEAM.
 * @Comments: 
 * =====================================================================
 *                   Lenovo Confidential Proprietary
 *                    Lenovo Beijing Limited
 *           (c) Copyright Lenovo 2010, All Rights Reserved
 *
 * Revision History:
 *
 *                   Modification  Tracking
 *
 * Author					Date            OS version        Reason 
 * ----------			------------     -------------     -----------
 * yueliang@use.com.cn   2010-11-17         LeOS2.0       Check for NULL, 0 h/w
 * =====================================================================
 **/
package com.adtworker.mail.customwidget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.SpinnerAdapter;

/**
 * 1. changed if expand when bInAnimating 2. changed if +1 when scroll to item
 * distance is 0
 * 
 * Object targetObj = mKeyWaiter.waitForNextEventTarget(null, qev, ev, true,
 * false); 3. on Scroll .on Fling
 * 
 * @author terryfee
 * 
 */
public class CoverflowEx extends AbsSpinnerEx
		implements
			GestureDetector.OnGestureListener {

	private boolean DEBUGFLUIDGALLERY = false;

	private SpinnerAdapter mAdapter;
	private int mSelectedPosition = 0;
	private int mOrigSelectedPosition = 0;
	private int mFirstPosition = 0;
	private GestureDetector mGestureDetector;

	private int MAX_VIEW_NUMBER = 1024;
	private FluidViewWrapper[] mWrapperList = new FluidViewWrapper[MAX_VIEW_NUMBER];
	private int mFluidWrapperCount = 0;

	private float mDistance = 0;

	private int mHeightMeasureSpec;
	private int mWidthMeasureSpec;

	private float mRadiusX = 0;
	private float mRadiusY = 0;
	private float mOrigRadiusX = 0;
	private float mOrigRadiusY = 0;

	// H
	private int mPaddingY = 0;
	private int mOrigPaddingY = 0;
	private int mSpaceX = 0;
	private int mGalleryDisplayWidth = 0;
	private int mGalleryDisplayLeft = 0;
	private int mGalleryDisplayRight = 0;
	private int mOffsetXFacter = 4;
	private boolean enableOffsetX = true;

	// V
	private int mPaddingX = 0;
	private int mOrigPaddingX = 0;
	private int mSpaceY = 0;
	private int mGalleryDisplayHeight = 0;
	private int mGalleryDisplayTop = 0;
	private int mGalleryDisplayBottom = 0;
	private int mOffsetYFacter = 6;
	private boolean enableOffsetY = true;
	private int mOrientation;

	private float mScale = 0;
	private float mOrigScale = 1;

	private float mScaleFactor = 0;
	private double mScaleExpr = 0;
	private double mOrigScaleExpr = 0.7;

	private boolean mShouldStopFling;

	private int mOrigCenterX = 0;
	private int mOrigCenterY = 0;
	private int centerY = 0;
	private int centerX = 0;

	private int mBufferedItemCount = 0;

	private int mDownTouchPosition = 0;

	private int autoScrollDuration = 1000;
	private FlingRunnable mFlingRunnable = new FlingRunnable();

	private boolean mShouldCallbackOnUnselectedItemClick = true;
	private View mDownTouchView;
	private FluidViewWrapper mDownTouchWrapper;

	public static int VERTICALMODE = 1;
	public static int HORIZONTALMODE = 0;
	public int mMode = HORIZONTALMODE;

	private boolean bExpanded = true;
	private boolean bInExpandAnimating = false;

	private int mOrigAutoScrollDuration = 1000;
	private int mOrigAutoExpandDuration = 500;
	private ExpandRunnable mExpandRunnable = new ExpandRunnable();

	private OnExpandListener mExpandListener = null;

	private OnScrollListener mScrollListener = null;

	private boolean bTouchable = true;

	private boolean bOneSlotEachScroll = false;

	private boolean mIsHorzCoverflow = true;

	private int mPrettyCXY = 0;
	private int mViewSizeNormal = 173;
	private float mViewSizeMaxLv = 1.780346821f;
	private float mViewSizeMinLv = 0.578034682f;
	private int mViewSizeMax = 0;
	private int mViewSizeMin = 0;

	private void fireCenterChanged() {
		if (mScrollListener != null) {
			int i = mSelectedPosition - mFirstPosition;
			FluidViewWrapper w = mWrapperList[i];
			if (w != null) {
				mScrollListener.onCenterChanged(mSelectedPosition, w.viewTag);
			}
		}
	}
	public void setSubViewNormalSize(int iNormalSize) {
		mViewSizeNormal = iNormalSize;
		mViewSizeMax = (int) (mViewSizeNormal * mViewSizeMaxLv);
		mViewSizeMin = (int) (mViewSizeNormal * mViewSizeMinLv);
	}

	public void setScrollOneSlot(boolean b) {
		// bOneSlotEachScroll = b;
	}

	public boolean isScrollOneSlot() {
		return bOneSlotEachScroll;
	}

	private int getChildMeasureEx(View v) {
		return mPrettyCXY;
		// if(mIsHorzCoverflow) return getHeight() * 2 / 3;
		// return getWidth() * 2 / 3;
	}

	private int getChildMeasureWidthEx(View v) {
		return getChildMeasureEx(v);
		// return v.getMeasuredWidth();
	}

	private int getChildMeasureHeightEx(View v) {
		return getChildMeasureEx(v);
		// return v.getMeasuredHeight();
	}

	public int getSelectedVPosition() {
		return mSelectedPosition;
	}

	public void setTouchable(boolean b) {
		bTouchable = b;
	}

	public void setMirrorEnable(boolean b) {

	}

	public void scrollToPosition_horz(int itemPosition) {
		if (mAdapter == null)
			return;
		if (itemPosition >= mAdapter.getCount())
			return;
		if (itemPosition == mSelectedPosition)
			setSelectionToCenterChild();
		else {
			int distance = (itemPosition - mSelectedPosition) * mSpaceX;
			setSelectionToCenterChild();
			mFlingRunnable.startUsingDistance_horz((-1) * distance, 500);
		}
	}

	public void scrollToPosition_vert(int itemPosition) {
		if (mAdapter == null)
			return;
		if (itemPosition >= mAdapter.getCount())
			return;
		if (itemPosition == mSelectedPosition)
			setSelectionToCenterChild();
		else {
			int distance = (itemPosition - mSelectedPosition) * mSpaceY;
			mFlingRunnable.startUsingDistance_vert((-1) * distance, 500);
		}
	}

	public void setVScrollCoverflow(boolean bVScroll) {
		if (bVScroll != mIsHorzCoverflow) {
			mIsHorzCoverflow = bVScroll;
			if (mIsHorzCoverflow) {
				calulate_pretty_cx();
			} else {
				calulate_pretty_cy();
			}
			invalidate();
		}
	}

	@Override
	public void onDetachedFromWindow() {
		recycleBitmaps();
		mAdapter = null;
		super.onDetachedFromWindow();
	}

	/*
	 * we simulate the distance
	 */
	public int getTotalDistance_horz() {
		int res = 0;
		if (mAdapter.getCount() <= 1)
			res = 0;
		else if (mBufferedItemCount < mAdapter.getCount())
			res = (mAdapter.getCount() - 1) * mSpaceX;
		else {
			res = mWrapperList[mFluidWrapperCount - 1].mX - mWrapperList[0].mX
					+ 10;
			// 10 is a appendix to avoid the prejustence.
		}
		return res;
	}

	public int getTotalDistance_vert() {
		int res = 0;
		if (mAdapter.getCount() <= 1)
			res = 0;
		else if (mBufferedItemCount < mAdapter.getCount())
			res = (mAdapter.getCount() - 1) * mSpaceY;
		else {
			res = mWrapperList[mFluidWrapperCount - 1].mY - mWrapperList[0].mY
					+ 10;
			// 10 is a appendix to avoid the prejustence.
		}
		return res;
	}

	public void scrollDistance_horz(boolean right, float percent) {
		if (percent <= 0 || percent > 1)
			return;
		if (mAdapter == null)
			return;
		if (mAdapter.getCount() <= 0)
			return;
		int distance = 0;

		int totalDistance = getTotalDistance_horz();
		distance = (int) (percent * totalDistance);
		if (right) {
			if (mSelectedPosition == 0) {
				FluidViewWrapper w = mWrapperList[0];
				if (w.mX >= centerX)
					return;
			}
			calculateLayouts_horz(distance);
		} else {
			if (mSelectedPosition == mAdapter.getCount() - 1) {
				FluidViewWrapper w = mWrapperList[mFluidWrapperCount - 1];
				if (w.mX <= centerX)
					return;
			}
			calculateLayouts_horz((-1) * distance);
		}
	}

	public void scrollDistance_vert(boolean right, float percent) {
		if (percent <= 0 || percent > 1)
			return;
		if (mAdapter == null)
			return;
		if (mAdapter.getCount() <= 0)
			return;

		int distance = 0;

		int totalDistance = getTotalDistance_vert();
		distance = (int) (percent * totalDistance);
		if (right) {
			if (mSelectedPosition == 0) {
				FluidViewWrapper w = mWrapperList[0];
				if (w.mY >= centerY)
					return;
			}
			calculateLayouts_vert(distance);
		} else {
			if (mSelectedPosition == mAdapter.getCount() - 1) {
				FluidViewWrapper w = mWrapperList[mFluidWrapperCount - 1];
				if (w.mY <= centerY)
					return;
			}
			calculateLayouts_vert((-1) * distance);
		}
	}

	public void setScrollListener(OnScrollListener listener) {
		mScrollListener = listener;
	}

	public void setExpandListener(OnExpandListener listener) {
		mExpandListener = listener;
	}

	public void setOrigAutoExpandDuration(int mSec) {
		mOrigAutoExpandDuration = mSec;
	}

	public void setExpanded(boolean b) {
		bExpanded = b;
	}

	private void initExpandParams() {
		for (int i = 0; i < mFluidWrapperCount; i++) {
			FluidViewWrapper w = mWrapperList[i];
			w.resetExpandParams();
		}
		if (mIsHorzCoverflow) {
			for (int i = mSelectedPosition - mFirstPosition; i < mFluidWrapperCount; i++) {
				FluidViewWrapper w = mWrapperList[i];

				boolean bNextNeedUpdate = w.updateExpandSize_horz();
				if (!bNextNeedUpdate)
					break;
			}
			for (int i = mSelectedPosition - mFirstPosition - 1; i >= 0; i--) {
				FluidViewWrapper w = mWrapperList[i];
				boolean bNextNeedUpdate = w.updateExpandSize_horz();
				if (!bNextNeedUpdate)
					break;
			}
		} else {
			for (int i = mSelectedPosition - mFirstPosition; i < mFluidWrapperCount; i++) {
				FluidViewWrapper w = mWrapperList[i];

				boolean bNextNeedUpdate = w.updateExpandSize_vert();
				if (!bNextNeedUpdate)
					break;
			}
			for (int i = mSelectedPosition - mFirstPosition - 1; i >= 0; i--) {
				FluidViewWrapper w = mWrapperList[i];
				boolean bNextNeedUpdate = w.updateExpandSize_vert();
				if (!bNextNeedUpdate)
					break;
			}
		}
	}

	public void expand() {
		if (bExpanded)
			return;
		if (bInExpandAnimating)
			return;
		if (mFluidWrapperCount <= 0)
			return;
		bInExpandAnimating = true;

		initExpandParams();

		bExpanded = true;
		if (mIsHorzCoverflow)
			expandLayout_1_horz(0);
		else
			expandLayout_1_vert(0);
		mExpandRunnable.expand();
		if (mExpandListener != null)
			mExpandListener.onExpandStart();
	}

	public void collapse() {
		if (DEBUGFLUIDGALLERY)
			Log.i("FLUIDGALLERY ", " COLLAPSE ************ " + bExpanded + "  "
					+ bInExpandAnimating);
		if (!bExpanded)
			return;
		if (bInExpandAnimating)
			return;
		if (mFluidWrapperCount <= 0)
			return;
		bInExpandAnimating = true;
		bExpanded = false;
		for (int i = 0; i < mFluidWrapperCount; i++) {
			FluidViewWrapper w = mWrapperList[i];
			w.resetExpandParams();
		}
		if (mIsHorzCoverflow) {
			for (int i = mSelectedPosition - mFirstPosition; i < mFluidWrapperCount; i++) {
				FluidViewWrapper w = mWrapperList[i];

				boolean bNextNeedUpdate = w.updateCollapseSize_horz();
				if (!bNextNeedUpdate)
					break;
			}
			for (int i = mSelectedPosition - mFirstPosition - 1; i >= 0; i--) {
				FluidViewWrapper w = mWrapperList[i];
				boolean bNextNeedUpdate = w.updateCollapseSize_horz();
				if (!bNextNeedUpdate)
					break;
			}
			expandLayout_2_horz(0);
		} else {
			for (int i = mSelectedPosition - mFirstPosition; i < mFluidWrapperCount; i++) {
				FluidViewWrapper w = mWrapperList[i];

				boolean bNextNeedUpdate = w.updateCollapseSize_vert();
				if (!bNextNeedUpdate)
					break;
			}
			for (int i = mSelectedPosition - mFirstPosition - 1; i >= 0; i--) {
				FluidViewWrapper w = mWrapperList[i];
				boolean bNextNeedUpdate = w.updateCollapseSize_vert();
				if (!bNextNeedUpdate)
					break;
			}
			expandLayout_2_vert(0);
		}
		mExpandRunnable.collapse();
		if (mExpandListener != null)
			mExpandListener.onCollapseStart();
	}

	private void expandLayout(int delta) {
		if (delta >= 0) {
			if (mIsHorzCoverflow)
				expandLayout_1_horz(delta);
			else
				expandLayout_1_vert(delta);
		} else if (delta < 0) {
			if (mIsHorzCoverflow)
				expandLayout_2_horz(delta);
			else
				expandLayout_2_vert(delta);
		}
	}

	/*
	 * expand process
	 */
	private void expandLayout_1_horz(int delta) {
		if (getChildCount() == 0) {
			return;
		}
		int rightborder = centerX + getWidth() / 2;
		int leftborder = centerX - getWidth() / 2;
		int size = mFluidWrapperCount;
		int layoutCount = 0;
		for (int i = size - 1; i >= 0; i--) {
			FluidViewWrapper w = mWrapperList[i];
			boolean bNeedLayout = true;
			if (w.bExpandToRight
					&& w.mExpandX < w.mExpandableFinalX
					&& w.mExpandableFinalX - w.mExpandWidth / 2
							+ w.mExpandOffsetX < rightborder)
				w.mExpandX += Math.min(delta, w.mExpandableFinalX - w.mExpandX);
			else if (!w.bExpandToRight
					&& w.mExpandX > w.mExpandableFinalX
					&& w.mExpandX + w.mExpandWidth / 2 + w.mExpandOffsetX > leftborder)
				w.mExpandX -= Math.min(delta, w.mExpandX - w.mExpandableFinalX);
			else
				bNeedLayout = false;
			View v = w.viewTag;
			if (w.bNeedDoExpand && bNeedLayout) {
				layoutCount++;
				v.layout(w.mExpandX - w.mExpandWidth / 2 + w.mExpandOffsetX,
						w.mExpandY - w.mExpandHeight / 2, w.mExpandX
								+ w.mExpandWidth / 2 + w.mExpandOffsetX,
						w.mExpandY + w.mExpandHeight / 2);
				if (w.hasMirror) {
					w.mirror.layout(w.mExpandX - w.mExpandWidth / 2
							+ w.mExpandOffsetX, w.mExpandY + w.mExpandHeight
							/ 2 + 10, w.mExpandX + w.mExpandWidth / 2
							+ w.mExpandOffsetX, w.mExpandY + w.mExpandHeight
							/ 2 + 10 + w.mExpandHeight);
				}
			} else {
				v.layout(w.mExpandX - w.mExpandWidth / 2 + w.mExpandOffsetX,
						w.mExpandY - w.mExpandHeight / 2, w.mExpandX
								+ w.mExpandWidth / 2 + w.mExpandOffsetX,
						w.mExpandY + w.mExpandHeight / 2);
				if (w.hasMirror) {
					w.mirror.layout(w.mExpandX - w.mExpandWidth / 2
							+ w.mExpandOffsetX, w.mExpandY + w.mExpandHeight
							/ 2 + 10, w.mExpandX + w.mExpandWidth / 2
							+ w.mExpandOffsetX, w.mExpandY + w.mExpandHeight
							/ 2 + 10 + w.mExpandHeight);
				}
			}

		}
		if (layoutCount == 0)
			mExpandRunnable.endFling(false);
		invalidate();
	}

	private void expandLayout_1_vert(int delta) {
		if (getChildCount() == 0) {
			return;
		}
		int bottomborder = centerY + getWidth() / 2;
		int topborder = centerY - getWidth() / 2;
		int size = mFluidWrapperCount;
		int layoutCount = 0;
		for (int i = size - 1; i >= 0; i--) {
			FluidViewWrapper w = mWrapperList[i];
			boolean bNeedLayout = true;
			if (w.bExpandToBottom
					&& w.mExpandY < w.mExpandableFinalY
					&& w.mExpandableFinalY - w.mExpandHeight / 2
							+ w.mExpandOffsetY < bottomborder)
				w.mExpandY += Math.min(delta, w.mExpandableFinalY - w.mExpandY);
			else if (!w.bExpandToBottom
					&& w.mExpandY > w.mExpandableFinalY
					&& w.mExpandY + w.mExpandHeight / 2 + w.mExpandOffsetY > topborder)
				w.mExpandY -= Math.min(delta, w.mExpandY - w.mExpandableFinalY);
			else
				bNeedLayout = false;
			View v = w.viewTag;
			if (w.bNeedDoExpand && bNeedLayout) {
				layoutCount++;
				v.layout(w.mExpandX - w.mExpandWidth / 2, w.mExpandY
						- w.mExpandHeight / 2 + w.mExpandOffsetY, w.mExpandX
						+ w.mExpandWidth / 2, w.mExpandY + w.mExpandHeight / 2
						+ w.mExpandOffsetY);
			} else {
				v.layout(w.mExpandX - w.mExpandWidth / 2, w.mExpandY
						- w.mExpandHeight / 2 + w.mExpandOffsetY, w.mExpandX
						+ w.mExpandWidth / 2, w.mExpandY + w.mExpandHeight / 2
						+ w.mExpandOffsetY);
			}

		}
		if (layoutCount == 0)
			mExpandRunnable.endFling(false);
		invalidate();
	}

	/*
	 * collapse process
	 */
	private void expandLayout_2_horz(int delta) {
		if (getChildCount() == 0) {
			return;
		}

		int size = mFluidWrapperCount;
		int rightborder = centerX + getWidth() / 2;
		int leftborder = centerX - getWidth() / 2;
		int layoutCount = 0;
		for (int i = size - 1; i >= 0; i--) {

			FluidViewWrapper w = mWrapperList[i];

			boolean bNeedLayout = true;

			if (w.bExpandToRight
					&& w.mExpandableFinalX < w.mExpandX
					&& w.mExpandX - w.mExpandWidth / 2 + w.mExpandOffsetX < rightborder)
				w.mExpandX += delta;
			else if (!w.bExpandToRight
					&& w.mExpandableFinalX > w.mExpandX
					&& (w.mExpandX + w.mExpandWidth / 2 + w.mExpandOffsetX) > leftborder)
				w.mExpandX -= delta;
			else
				bNeedLayout = false;
			View v = w.viewTag;
			if (w.bNeedDoExpand && bNeedLayout) {
				layoutCount++;

				v.layout(w.mExpandX - w.mExpandWidth / 2 + w.mExpandOffsetX,
						w.mExpandY - w.mExpandHeight / 2, w.mExpandX
								+ w.mExpandWidth / 2 + w.mExpandOffsetX,
						w.mExpandY + w.mExpandHeight / 2);
				if (w.hasMirror) {
					w.mirror.layout(w.mExpandX - w.mExpandWidth / 2
							+ w.mExpandOffsetX, w.mExpandY + w.mExpandHeight
							/ 2 + 10, w.mExpandX + w.mExpandWidth / 2
							+ w.mExpandOffsetX, w.mExpandY + w.mExpandHeight
							/ 2 + 10 + w.mExpandHeight);
				}
			} else {
				v.layout(w.mExpandableFinalX - w.mExpandWidth / 2, w.mExpandY
						- w.mExpandHeight / 2, w.mExpandableFinalX
						+ w.mExpandWidth / 2, w.mExpandY + w.mExpandHeight / 2);
				if (w.hasMirror) {
					w.mirror.layout(w.mExpandableFinalX - w.mExpandWidth / 2,
							w.mExpandableFinalY + w.mExpandHeight / 2 + 10,
							w.mExpandableFinalX + w.mExpandWidth / 2,
							w.mExpandableFinalY + w.mExpandHeight / 2 + 10
									+ w.mExpandHeight);
				}
			}
		}
		if (layoutCount == 0)
			mExpandRunnable.endFling(false);
		invalidate();
	}

	private void expandLayout_2_vert(int delta) {
		if (getChildCount() == 0) {
			return;
		}

		int size = mFluidWrapperCount;
		int bottomborder = centerX + getWidth() / 2;
		int topborder = centerX - getWidth() / 2;
		int layoutCount = 0;
		for (int i = size - 1; i >= 0; i--) {

			FluidViewWrapper w = mWrapperList[i];

			boolean bNeedLayout = true;

			if (w.bExpandToBottom
					&& w.mExpandableFinalY < w.mExpandY
					&& w.mExpandY - w.mExpandHeight / 2 + w.mExpandOffsetY < bottomborder)
				w.mExpandY += delta;
			else if (!w.bExpandToBottom
					&& w.mExpandableFinalY > w.mExpandY
					&& (w.mExpandY + w.mExpandHeight / 2 + w.mExpandOffsetY) > topborder)
				w.mExpandY -= delta;
			else
				bNeedLayout = false;
			View v = w.viewTag;
			if (w.bNeedDoExpand && bNeedLayout) {
				layoutCount++;

				v.layout(w.mExpandX - w.mExpandWidth / 2, w.mExpandY
						- w.mExpandHeight / 2 + w.mExpandOffsetY, w.mExpandX
						+ w.mExpandWidth / 2, w.mExpandY + w.mExpandHeight / 2
						+ w.mExpandOffsetY);

			} else {
				v.layout(w.mExpandableFinalX - w.mExpandWidth / 2, w.mExpandY
						- w.mExpandHeight / 2, w.mExpandableFinalX
						+ w.mExpandWidth / 2, w.mExpandY + w.mExpandHeight / 2);

			}
		}
		if (layoutCount == 0)
			mExpandRunnable.endFling(false);
		invalidate();
	}

	public interface OnExpandListener {
		public void onExpandStart();

		public void onExpandEnd();

		public void onCollapseStart();

		public void onCollapseEnd();

	}

	public interface OnScrollListener {
		public void onCenterChanged(int iSel, View v);

	}

	private class ExpandRunnable implements Runnable {
		/**
		 * Tracks the decay of a fling scroll
		 */
		private Scroller mScroller;

		/**
		 * X value reported by mScroller on the previous fling
		 */
		private int mLastFlingX;
		private int mLastFlingY;

		public ExpandRunnable() {
			mScroller = new Scroller(getContext(), new LinearInterpolator());
		}

		private void startCommon() {
			// Remove any pending flings
			removeCallbacks(this);
		}

		private void expand() {
			startCommon();
			startUsingDistance((getWidth() / 2 + getPaddingRight()));
		}

		private void collapse() {
			startCommon();
			startUsingDistance(((-1) * (getWidth() / 2 + getPaddingRight())));
		}

		public void startUsingDistance(int distance) {
			if (distance == 0)
				return;

			startCommon();

			mLastFlingX = 0;
			mLastFlingY = 0;

			mScroller.startScroll(0, 0, -distance, 0, mOrigAutoExpandDuration);
			post(this);
		}

		public void stop(boolean scrollIntoSlots) {
			removeCallbacks(this);
			// Log.i("fling","stop *******************");
			endFling(scrollIntoSlots);
		}

		private void endFling(boolean scrollIntoSlots) {
			/*
			 * Force the scroller's status to finished (without setting its
			 * position to the end)
			 */
			if (!bInExpandAnimating)
				return;
			mScroller.forceFinished(true);
			bInExpandAnimating = false;
			// Log.i("Fling","end fling *******************");
			if (mExpandListener != null) {
				if (bExpanded)
					mExpandListener.onExpandEnd();
				else
					mExpandListener.onCollapseEnd();
			}
		}

		@Override
		public void run() {
			if (mFluidWrapperCount == 0) {
				endFling(true);
				return;
			}

			mShouldStopFling = false;

			if (mIsHorzCoverflow) {
				final Scroller scroller = mScroller;

				boolean more = scroller.computeScrollOffset();
				final int x = scroller.getCurrX();

				// Flip sign to convert finger direction to list items direction
				// (e.g. finger moving down means list is moving towards the
				// top)
				int delta = mLastFlingX - x;

				// delta = Math.min(delta,(getWidth() - getPaddingLeft() -
				// getPaddingRight())/4 + 1);
				if (delta != 0)
					expandLayout(delta);

				if (more && !mShouldStopFling) {
					mLastFlingX = x;
					post(this);
				} else {
					endFling(true);
				}
			} else {
				final Scroller scroller = mScroller;
				boolean more = scroller.computeScrollOffset();
				final int y = scroller.getCurrY();

				// Flip sign to convert finger direction to list items direction
				// (e.g. finger moving down means list is moving towards the
				// top)
				int delta = mLastFlingY - y;

				// delta = Math.min(delta,(getWidth() - getPaddingLeft() -
				// getPaddingRight())/4 + 1);
				if (delta != 0)
					expandLayout(delta);

				if (more && !mShouldStopFling) {
					mLastFlingY = y;
					post(this);
				} else {
					endFling(true);
				}
			}
		}

	}

	/*
	 * Set the X radius, the default value is calculated as v.measuredWidth *
	 * mBufferedItemCount * 4/5 , mBufferedItemCount is the item count that
	 * watchable on the screen
	 * 
	 * @param x
	 * 
	 * @Return void
	 */
	public void setRadiusX(int x) {
		mRadiusX = x;
	}

	/*
	 * Set the Y radius , used by inner function
	 * 
	 * @param y
	 * 
	 * @Return void
	 */
	private void setRadiusY(int y) {
		mRadiusY = y;
	}

	/*
	 * Set the scale for the view , the view in the center is size of scale *
	 * v.measuredWidth
	 * 
	 * @param double f
	 * 
	 * @return void
	 */
	public void setScale(double f) {
		mOrigScale = (float) f;
	}

	/*
	 * get scale value
	 */
	public float getScale() {
		return mOrigScale;
	}

	/*
	 * set the scale expr , when 0 < f < 1.0 , the size is from small to large
	 * as from sides to center , the smaller this count , the faster the size
	 * changes
	 * 
	 * @param double f
	 * 
	 * @return void
	 */
	public void setScaleExpr(double f) {
		mScaleExpr = f;
	}

	// v
	/*
	 * set padding between each views listed on the screen , user can change
	 * this to adjust the function
	 * 
	 * @param int padding
	 * 
	 * @return void
	 */
	public void setPaddingX(int padding) {
		mPaddingX = padding;
	}

	/*
	 * get paddigng Y
	 */
	public int getPaddingX() {
		return mPaddingX;
	}

	// h
	/*
	 * set padding between each views listed on the screen , user can change
	 * this to adjust the function
	 * 
	 * @param int padding
	 * 
	 * @return void
	 */
	public void setPaddingY(int padding) {
		mPaddingY = padding;
	}

	/*
	 * get paddigng Y
	 */
	public int getPaddingY() {
		return mPaddingY;
	}

	/*
	 * set auto scroll duration , 1000msec as default
	 * 
	 * @param int mSec
	 * 
	 * @return void
	 */
	public void setAutoScrollAnimationDuration(int mSec) {
		if (mSec > 0)
			mOrigAutoScrollDuration = mSec;
		else
			mOrigAutoScrollDuration = 1000;
	}

	/*
	 * get auto scroll duration
	 * 
	 * @return res
	 */
	public int getAutoScrollAnimationDuration() {
		return mOrigAutoScrollDuration;
	}

	private void setDisplayItemCount(int count) {
		mBufferedItemCount = count;
	}

	/*
	 * set start selected position ,default is 0
	 * 
	 * @param int index
	 * 
	 * @return void
	 */
	public void setStartupSelectedPosition(int index) {
		if (index >= 0)
			mOrigSelectedPosition = index;
		else
			mOrigSelectedPosition = 0;
	}

	@Override
	public void setSelection(int position, boolean animate) {
		mSelectedPosition = position;
		super.setSelection(position, animate);
		mSelectedPosition = position;
		requestLayout();
		invalidate();
	}

	@Override
	public void setSelection(int position) {
		int iSel = getSelectedItemPosition();
		super.setSelection(position);
		mSelectedPosition = position;
		requestLayout();
		invalidate();
	}

	// v
	private void setOffsetXFactor(int f) {
		mOffsetXFacter = f;
	}

	private void enableOffsetX(boolean b) {
		enableOffsetX = b;
	}

	// h
	private void setOffsetYFactor(int f) {
		mOffsetYFacter = f;
	}

	private void enableOffsetY(boolean b) {
		enableOffsetY = b;
	}

	/*
	 * set if select item also cause a item click
	 * 
	 * @param booelan b
	 * 
	 * @return void
	 */
	public void setCallbackOnUnselectedItemClick(boolean shouldCallback) {
		mShouldCallbackOnUnselectedItemClick = shouldCallback;
	}

	/*
	 * set center of y
	 */
	public void setCenterY(int y) {
		mOrigCenterY = y;
	}

	/*
	 * get Center of y
	 * 
	 * @return float
	 */
	public float getCenterY() {
		return mOrigCenterY;
	}

	/*
	 * set center of x
	 */
	public void setCenterX(int x) {
		mOrigCenterX = x;
	}

	/*
	 * get center of x
	 * 
	 * @return float
	 */
	public int getCenterX() {
		return mOrigCenterX;
	}

	/*
	 * set scale expr
	 */
	public void setOrigScaleExpr(double expr) {
		mOrigScaleExpr = expr;
	}

	/*
	 * get orig scale expr
	 */
	public double getOrigScaleExpr() {
		return mOrigScaleExpr;
	}

	/*
	 * reset all the params
	 * 
	 * @return void
	 */
	public void resetParams() {
		mRadiusX = mOrigRadiusX;
		mRadiusY = mOrigRadiusY;
		mScale = mOrigScale;
		mScaleExpr = mOrigScaleExpr;
		// v
		mPaddingX = mOrigPaddingX;
		// h
		mPaddingY = mOrigPaddingY;
		mBufferedItemCount = 0;
		autoScrollDuration = mOrigAutoScrollDuration;
		mSelectedPosition = mOrigSelectedPosition;
		centerY = mOrigCenterY;
		centerX = mOrigCenterX;

		mOrientation = getContext().getResources().getConfiguration().orientation;
	}

	/*
	 * constructor
	 * 
	 * @return void
	 */
	public CoverflowEx(Context context) {
		this(context, null);
		// mGroupFlags |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;
	}

	public CoverflowEx(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CoverflowEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, 0);

		mGestureDetector = new GestureDetector(this);
		mGestureDetector.setIsLongpressEnabled(true);
		setChildrenDrawingOrderEnabled(true);
		setStaticTransformationsEnabled(true);
		setSubViewNormalSize(173);
		// mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;
		// mGroupFlags |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;
	}

	@Override
	public ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	private FluidViewWrapper makeAndAddView(int itemPosition,
			int layoutPosition, int listPosition) {
		View v = mAdapter.getView(itemPosition, null, this);
		v.setId(itemPosition);
		// /ViewGroup.LayoutParams lp = generateDefaultLayoutParams();
		CoverflowEx.LayoutParams lp = v.getLayoutParams();
		if (lp == null) {
			lp = generateDefaultLayoutParams();
		}
		addViewInLayout(v, layoutPosition, lp);
		int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
				getPaddingTop() + getPaddingRight(), lp.height);
		int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
				getPaddingLeft() + getPaddingRight(), lp.width);
		v.measure(childWidthSpec, childHeightSpec);
		FluidViewWrapper w = new FluidViewWrapper(v, itemPosition, listPosition);
		try {
			addWrapperInArray(w, listPosition);
		} catch (IndexOutOfBoundsException e) {
			Log.i("RocketMusicHCoverflow", "Add Wrapper Out of bounds");
		}
		return w;
	}

	private void addWrapperInArray(FluidViewWrapper w, int index) {
		FluidViewWrapper[] children = mWrapperList;
		final int count = mFluidWrapperCount;
		final int size = children.length;
		if (index == count) {
			// expand the array
			if (size == count) {
				mWrapperList = new FluidViewWrapper[size + MAX_VIEW_NUMBER];
				System.arraycopy(children, 0, mWrapperList, 0, size);
				children = mWrapperList;
			}
			children[mFluidWrapperCount++] = w;
		} else if (index < count) {
			if (size == count) {
				mWrapperList = new FluidViewWrapper[size + MAX_VIEW_NUMBER];
				System.arraycopy(children, 0, mWrapperList, 0, index);
				System.arraycopy(children, index, mWrapperList, index + 1,
						count - index);
				children = mWrapperList;
			} else {
				System.arraycopy(children, index, children, index + 1, count
						- index);
			}
			children[index] = w;
			mFluidWrapperCount++;
		} else {
			throw new IndexOutOfBoundsException("index=" + index + " count="
					+ count);
		}

		FluidViewWrapper v = mWrapperList[mFluidWrapperCount - 1];
	}

	private void removeWrapperFromArray(int index) {
		final FluidViewWrapper[] children = mWrapperList;
		final int count = mFluidWrapperCount;
		if (index == count - 1) {
			children[--mFluidWrapperCount] = null;
		} else if (index >= 0 && index < count) {
			System.arraycopy(children, index + 1, children, index, count
					- index - 1);
			children[--mFluidWrapperCount] = null;
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	private void removeWrapperFromArray(int start, int count) {
		final FluidViewWrapper[] children = mWrapperList;
		final int childrenCount = mFluidWrapperCount;

		start = Math.max(0, start);
		final int end = Math.min(childrenCount, start + count);

		if (start == end) {
			return;
		}

		if (end == childrenCount) {
			for (int i = start; i < end; i++) {
				children[i] = null;
			}
		} else {
			System.arraycopy(children, end, children, start, childrenCount
					- end);
			for (int i = childrenCount - (end - start); i < childrenCount; i++) {
				children[i] = null;
			}
		}

		mFluidWrapperCount -= (end - start);
		FluidViewWrapper w = mWrapperList[mFluidWrapperCount - 1];
	}

	// private boolean hasRecycled = false;
	public void recycleBitmaps() {
		// if(hasRecycled)
		// return;
		// Log.i("onDown","mFlingRunnable.stop 1184....");
		// mFlingRunnable.stop(false);
		// mExpandRunnable.stop(false);
		/*
		 * int count = getChildCount(); for (int i = 0; i < count; i++) { View
		 * child = getChildAt(i); if (child instanceof ImageView) { ImageView v
		 * = (ImageView) child; BitmapDrawable bitmapDrawable = (BitmapDrawable)
		 * v .getDrawable(); Bitmap map = bitmapDrawable.getBitmap(); if (map !=
		 * null) map.recycle(); } }
		 */
		// removeAllViews();
		// hasRecycled = true;
	}

	private boolean bDataChanged = false;

	@Override
	public void setAdapter(SpinnerAdapter adapter) {
		mAdapter = adapter;
		bDataChanged = true;
		super.setAdapter(adapter);
	}

	private void calulate_pretty_cx() {
		// float scale = (float) (mScaleFactor * Math.pow(mSpaceX *2,
		// mScaleExpr));
		// int iPretty = (int)((mScale - getWidth() - getPaddingRight() -
		// centerX - mSpaceX *2)*2/scale);
		// int iPretty2 = getHeight() * 2 / 3;
		// mPrettyCXY = iPretty > iPretty2 ? iPretty2:iPretty;
		// mRadiusX = measuredWidth * mBufferedItemCount * 2 / 5;
		// mSpaceY = (int) (mRadiusY / mBufferedItemCount);
		// int lastX = w.mX;
		// w.setX(lastX + mSpaceX);
		// mSpaceY = measuredWidth * 2/5;
		// scale = (float) (mScale - mScaleFactor * Math.pow(gap, mScaleExpr));

		// measuredWidth*scale/2 + mSpaceX*2 + getCenterXOfGallery =
		// getWidth()-getPaddingRight();
		// measuredWidth * scale/2 + measuredWidth *2/5 *2 +
		// getCenterXOfGallery() = getWidth()-getPaddingRight();

		// measuredWidth =( getWidth()-getPaddingRight() - getCenterXOfGallery()
		// )/(scale/2 + 4/5)
		float scale = (1.0f);
		int iPretty = (int) ((getWidth() - getPaddingRight() - getCenterXOfGallery()) / (scale / 2.0f + 0.8f));
		int iPretty2 = getHeight();
		mPrettyCXY = iPretty > iPretty2 ? iPretty2 : iPretty;
		if (mPrettyCXY > mViewSizeMax)
			mPrettyCXY = mViewSizeMax;
		else if (mPrettyCXY < mViewSizeMin)
			mPrettyCXY = mViewSizeMin;
	}

	private void calulate_pretty_cy() {
		float scale = (1.0f);
		int iPretty = (int) ((getHeight() - getPaddingBottom() - getCenterYOfGallery()) / (scale / 2.0f + 0.8f));
		int iPretty2 = getWidth();
		mPrettyCXY = iPretty > iPretty2 ? iPretty2 : iPretty;
		if (mPrettyCXY > mViewSizeMax)
			mPrettyCXY = mViewSizeMax;
		else if (mPrettyCXY < mViewSizeMin)
			mPrettyCXY = mViewSizeMin;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if (mAdapter == null || mAdapter.getCount() <= 0)
			return;
		final int oldSelection = mSelectedPosition;
		// Log.i("Layout " , " ************ " + mOrigSelectedPosition + "  " +
		// mSelectedPosition + " " + bDataChanged);
		resetParams();
		if (!bDataChanged)
			mSelectedPosition = oldSelection;
		else
			bDataChanged = false;

		if (mIsHorzCoverflow) {
			calulate_pretty_cx();
		} else {
			calulate_pretty_cy();
		}

		// we need to get the first view's measured height
		View firstView = mAdapter.getView(0, null, this);
		ViewGroup.LayoutParams lp = generateDefaultLayoutParams();
		/*
		 * // ViewGroup.LayoutParams lp = generateDefaultLayoutParams();
		 * RocketMusicHCoverflow.LayoutParams lp =
		 * (RocketMusicHCoverflow.LayoutParams) firstView .getLayoutParams(); if
		 * (lp == null) { lp = (RocketMusicHCoverflow.LayoutParams)
		 * generateDefaultLayoutParams(); }
		 */
		int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
				getPaddingTop() + getPaddingBottom(), lp.height);
		int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
				getPaddingLeft() + getPaddingRight(), lp.width);

		firstView.measure(childWidthSpec, childHeightSpec);
		// int measuredWidth = firstView.getMeasuredWidth();// we need to use
		// int measuredHeight = firstView.getMeasuredHeight();
		int measuredWidth = getChildMeasureWidthEx(firstView);
		int measuredHeight = getChildMeasureHeightEx(firstView);
		if (measuredWidth == 0)
			measuredWidth = 1;
		if (measuredHeight == 0)
			measuredHeight = 1;
		// init params
		int itemCount = mAdapter.getCount();
		// if (centerX == 0)
		centerX = getCenterXOfGallery();
		// if (centerY == 0)
		centerY = getCenterYOfGallery();
		if (mIsHorzCoverflow) {
			// h
			mGalleryDisplayWidth = 3 * (getWidth() + getPaddingLeft() + getPaddingRight());
			mGalleryDisplayLeft = centerX - mGalleryDisplayWidth / 2;
			mGalleryDisplayRight = centerX + mGalleryDisplayWidth / 2;

			// if (mBufferedItemCount == 0) {
			if (mGalleryDisplayWidth / measuredWidth % 2 == 0)
				mBufferedItemCount = mGalleryDisplayWidth / measuredWidth + 1;
			else
				mBufferedItemCount = mGalleryDisplayWidth / measuredWidth;
			// }

			// if (mRadiusX == 0)
			mRadiusX = measuredWidth * mBufferedItemCount * 2 / 5;
			// if (mRadiusY == 0)
			mRadiusY = 20 * itemCount;
			// if (mScale == 0)
			mScale = (float) 1.0;
			// if (mScaleExpr == 0)
			mScaleExpr = 0.7;
			double tmp = Math.pow(mRadiusX, mScaleExpr);
			mScaleFactor = (float) (mScale / tmp);

			calulate_pretty_cx();

			setCenterToInt_horz(mSelectedPosition);
		} else {
			mGalleryDisplayHeight = 3 * (getHeight() + getPaddingTop() + getPaddingBottom());
			mGalleryDisplayTop = centerY - mGalleryDisplayHeight / 2;
			mGalleryDisplayBottom = centerY + mGalleryDisplayHeight / 2;
			// if (mBufferedItemCount == 0) {
			if (mGalleryDisplayHeight / measuredHeight % 2 == 0)
				mBufferedItemCount = mGalleryDisplayHeight / measuredHeight + 1;
			else
				mBufferedItemCount = mGalleryDisplayHeight / measuredHeight;
			// }
			// if (mRadiusY == 0)
			// mRadiusY = measuredHeight * mBufferedItemCount / 5;
			mRadiusY = measuredHeight * mBufferedItemCount * 2 / 5;
			// if (mRadiusX == 0)
			mRadiusX = 20 * itemCount;

			// if (mScale == 0)
			// mScale = (float) 2.0;
			mScale = (float) 1.0;
			// if (mScaleExpr == 0)
			// mScaleExpr = 1.0;
			mScaleExpr = 0.7;
			double tmp = Math.pow(mRadiusY, mScaleExpr);
			mScaleFactor = (float) (mScale / tmp);
			calulate_pretty_cy();

			setCenterToInt_vert(mSelectedPosition);
		}
	}

	public void setCenterToInt_horz(int index) {
		int count = getChildCount();
		recycleBitmaps();
		recycleAllViews();
		detachAllViewsFromParent();
		for (int i = 0; i < mFluidWrapperCount; i++)
			mWrapperList[i] = null;
		mFluidWrapperCount = 0;
		mSelectedPosition = index;

		int itemCount = mAdapter.getCount();
		if (mSelectedPosition >= itemCount) {
			mSelectedPosition = itemCount - 1;
		}
		// if(mScrollListener!=null)
		// mScrollListener.onCenterChanged(mSelectedPosition);

		int leftItems = mSelectedPosition - (mBufferedItemCount - 1) / 2;
		if (leftItems >= 0)
			mFirstPosition = leftItems;
		else
			mFirstPosition = 0;

		// Log.i("FLuidGallery","set center to int **************** " +
		// mFluidWrapperCount + "  " + itemCount + " " + mBufferedItemCount);

		for (int i = 0; i < mBufferedItemCount
				&& (mFirstPosition + i) < itemCount; i++) {
			FluidViewWrapper w = makeAndAddView(mFirstPosition + i, i, i);
		}

		if (mFluidWrapperCount <= 0)
			return;
		initXPos();

		if (bExpanded)
			layout_horz();
		else {
			initExpandParams();
			expandLayout(0);
		}
	}

	public void setCenterToInt_vert(int index) {
		// only speical for Rocket Cover flow widget
		recycleBitmaps();
		recycleAllViews();
		detachAllViewsFromParent();
		for (int i = 0; i < mFluidWrapperCount; i++)
			mWrapperList[i] = null;
		mFluidWrapperCount = 0;
		mSelectedPosition = index;

		int itemCount = mAdapter.getCount();
		if (mSelectedPosition >= itemCount) {
			mSelectedPosition = itemCount - 1;
		}
		// if(mScrollListener!=null)
		// mScrollListener.onCenterChanged(mSelectedPosition);

		int leftItems = mSelectedPosition - (mBufferedItemCount - 1) / 2;
		if (leftItems >= 0)
			mFirstPosition = leftItems;
		else
			mFirstPosition = 0;

		for (int i = 0; i < mBufferedItemCount
				&& (mFirstPosition + i) < itemCount; i++) {

			FluidViewWrapper w = makeAndAddView(mFirstPosition + i, i, i);
		}
		if (mFluidWrapperCount <= 0)
			return;
		initYPos();

		if (bExpanded)
			layout_vert();
		else {
			initExpandParams();
			expandLayout(0);
		}
	}

	private void initXPos() {
		int itemCount = mFluidWrapperCount;
		FluidViewWrapper v = mWrapperList[mFluidWrapperCount - 1];

		mSpaceX = (int) (mRadiusX / mBufferedItemCount);

		int gap = mSelectedPosition - mFirstPosition;
		for (int i = gap - 1; i >= 0; i--) {
			FluidViewWrapper w = mWrapperList[i];
			w.setX(centerX - mSpaceX * (gap - i));
			w.calculatePos_horz();
		}

		for (int j = gap; j < mBufferedItemCount && j < mFluidWrapperCount; j++) {
			FluidViewWrapper w = mWrapperList[j];
			if (j == mSelectedPosition - mFirstPosition) {
				w.setX(centerX);
			} else {
				w.setX(centerX + mSpaceX * (j - gap));
			}
			w.calculatePos_horz();
		}
	}

	private void initYPos() {
		int itemCount = mFluidWrapperCount;
		FluidViewWrapper v = mWrapperList[mFluidWrapperCount - 1];

		mSpaceY = (int) (mRadiusY / mBufferedItemCount);

		int gap = mSelectedPosition - mFirstPosition;
		for (int i = gap - 1; i >= 0; i--) {
			FluidViewWrapper w = mWrapperList[i];
			w.setY(centerY - mSpaceY * (gap - i));
			w.calculatePos_vert();
		}

		for (int j = gap; j < mBufferedItemCount && j < mFluidWrapperCount; j++) {
			FluidViewWrapper w = mWrapperList[j];
			if (j == mSelectedPosition - mFirstPosition) {
				w.setY(centerY);
			} else {
				w.setY(centerY + mSpaceY * (j - gap));
			}
			w.calculatePos_vert();
		}
	}

	private void layout_horz() {
		if (getChildCount() == 0) {
			return;
		}

		int size = mFluidWrapperCount;
		for (int i = size - 1; i >= 0; i--) {
			FluidViewWrapper w = mWrapperList[i];
			View v = w.viewTag;
			if (w.bNeedLayout) {
				v.layout(w.mX - w.mWidth / 2 + w.offsetX, w.mY - w.mHeight / 2,
						w.mX + w.mWidth / 2 + w.offsetX, w.mY + w.mHeight / 2);
				if (w.hasMirror) {
					w.mirror.layout(w.mX - w.mWidth / 2 + w.offsetX, w.mY
							+ w.mHeight / 2 + 10, w.mX + w.mWidth / 2
							+ w.offsetX, w.mY + w.mHeight / 2 + 10 + w.mHeight);
				}
			}

		}
		invalidate();
	}

	private void layout_vert() {
		if (getChildCount() == 0) {
			return;
		}

		int size = mFluidWrapperCount;
		for (int i = size - 1; i >= 0; i--) {
			FluidViewWrapper w = mWrapperList[i];
			View v = w.viewTag;
			if (w.bNeedLayout) {
				v.layout(w.mX - w.mWidth / 2, w.mY - w.mHeight / 2 + w.offsetY,
						w.mX + w.mWidth / 2, w.mY + w.mHeight / 2 + w.offsetY);
			}

		}
		invalidate();
	}

	/*
	 * get the centered child position
	 * 
	 * @return int
	 */
	public int getCenterChildPosition() {
		return mSelectedPosition;
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		t.clear();
		if (mIsHorzCoverflow) {
			if (child.getLeft() < getPaddingLeft()
					|| child.getRight() > (getRight() - getPaddingRight())) {
				t.setAlpha(0.0f);
				return true;
			}
		} else {
			if (child.getTop() < getPaddingTop()
					|| child.getBottom() > (getBottom() - getPaddingBottom())) {
				t.setAlpha(0.0f);
				return true;
			}
		}
		return false;

		/*
		 * //the flollowing is the old code,and comment by wangcan for support
		 * 3,5,or more items float alpha; int myId = child.getId(); int center =
		 * mSelectedPosition;
		 * 
		 * // if(mOrientation == Configuration.ORIENTATION_PORTRAIT && (myId ==
		 * // center - 3 || myId == center + 3)) // return false; if (myId ==
		 * center - 1 || myId == center + 1) return false; // alpha = 0.7f; else
		 * if (myId == center - 2 || myId == center + 2) return false; // alpha
		 * = 0.3f; else if (myId == center) return false;// alpha = 1.0f; else
		 * alpha = 0.0f; t.setAlpha(alpha); return true;// true;
		 */

	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if (i == childCount - 1)
			return mSelectedPosition - mFirstPosition;
		else if (i >= mSelectedPosition - mFirstPosition) {
			int tmp = i - (mSelectedPosition - mFirstPosition);
			return childCount - 1 - tmp;
		} else
			return i;
	}

	private int getCenterYOfGallery() {
		return (getHeight() - getPaddingTop() - getPaddingBottom()) / 2
				+ getPaddingTop();
	}

	private int getCenterXOfGallery() {

		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2
				+ getPaddingLeft();
	}

	class FluidViewWrapper {
		private View viewTag;
		private ImageView mirror;
		private boolean hasMirror;
		private Bitmap thumbnail;

		private int mItemPosition;
		private int mLayoutPosition;
		private int mSelectPosition;
		private int mPos;
		private float scale = 1;
		private int mX;
		private int mY;
		private int offsetX;
		private int offsetY;
		private int mWidth;
		private int mHeight;
		private int origWidth;
		private int origHeight;

		private int mExpandX;
		private int mExpandY;
		private int mExpandableFinalX;
		private int mExpandableFinalY;
		private int mExpandOffsetX;
		private int mExpandOffsetY;
		private int mExpandWidth;
		private int mExpandHeight;
		private boolean bExpandToRight;
		private boolean bExpandToBottom;
		private boolean bNeedDoExpand = false;

		private void resetExpandParams() {
			mExpandX = centerX;
			mExpandY = centerY;
			mExpandableFinalX = centerX;
			mExpandableFinalY = centerY;
			mExpandWidth = 0;
			mExpandHeight = 0;
			bNeedDoExpand = false;
			mExpandOffsetX = 0;
			mExpandOffsetY = 0;
		}

		private boolean updateExpandSize_horz() {
			int rightborder = centerX + getWidth() / 2;
			int leftborder = centerX - getWidth() / 2;
			bExpandToRight = (mItemPosition > mSelectedPosition) ? true : false;
			if (mSpaceX == 0)
				mSpaceX = (int) mRadiusX / mBufferedItemCount;
			mExpandX = centerX;
			mExpandableFinalX = centerX + (mItemPosition - mSelectedPosition)
					* mSpaceX;
			int gap = Math.abs(centerX - mExpandableFinalX);
			mExpandY = mExpandableFinalY = (int) (centerY - mPaddingY * gap
					/ (mRadiusX / mBufferedItemCount));

			scale = (float) (mScale - mScaleFactor * Math.pow(gap, mScaleExpr));
			mExpandWidth = (int) (origWidth * scale);
			mExpandHeight = (int) (origHeight * scale);
			calcluateExpandOffset_horz();
			bNeedDoExpand = true;
			if (bExpandToRight)
				return (mExpandableFinalX + mExpandWidth / 2 + mExpandOffsetX <= rightborder);
			else
				return (mExpandableFinalX - mExpandWidth / 2 + mExpandOffsetX >= leftborder);
		}

		private boolean updateExpandSize_vert() {
			int topborder = centerY + getHeight() / 2;
			int bottomborder = centerY - getHeight() / 2;
			bExpandToBottom = (mItemPosition > mSelectedPosition)
					? true
					: false;
			if (mSpaceY == 0)
				mSpaceY = (int) mRadiusY / mBufferedItemCount;
			mExpandY = centerY;
			mExpandableFinalY = centerY + (mItemPosition - mSelectedPosition)
					* mSpaceY;

			int gap = Math.abs(centerX - mExpandableFinalY);
			mExpandX = mExpandableFinalX = (int) (centerX - mPaddingX * gap
					/ (mRadiusY / mBufferedItemCount));

			scale = (float) (mScale - mScaleFactor * Math.pow(gap, mScaleExpr));
			mExpandWidth = (int) (origWidth * scale);
			mExpandHeight = (int) (origHeight * scale);
			calcluateExpandOffset_vert();
			bNeedDoExpand = true;
			if (bExpandToBottom)
				return (mExpandableFinalY + mExpandHeight / 2 + mExpandOffsetY <= topborder);
			else
				return (mExpandableFinalY + mExpandHeight / 2 + mExpandOffsetY >= bottomborder);
		}

		private boolean updateCollapseSize_horz() {
			int rightborder = centerX + getWidth() / 2;
			int leftborder = centerX - getWidth() / 2;

			bExpandToRight = (mItemPosition > mSelectedPosition) ? true : false;
			if (mSpaceX == 0)
				mSpaceX = (int) mRadiusX / mBufferedItemCount;
			mExpandableFinalX = centerX;
			mExpandX = centerX + (mItemPosition - mSelectedPosition) * mSpaceX;
			int gap = Math.abs(centerX - mExpandX);
			mExpandY = mExpandableFinalY = (int) (centerY - mPaddingY * gap
					/ (mRadiusX / mBufferedItemCount));

			scale = (float) (mScale - mScaleFactor * Math.pow(gap, mScaleExpr));
			mExpandWidth = (int) (origWidth * scale);
			mExpandHeight = (int) (origHeight * scale);
			calcluateExpandOffset_horz();
			bNeedDoExpand = true;
			if (bExpandToRight)
				return (mExpandX + mExpandWidth / 2 + mExpandOffsetX <= rightborder);
			else
				return (mExpandX - mExpandWidth / 2 + mExpandOffsetX >= leftborder);
		}

		private boolean updateCollapseSize_vert() {
			int topborder = centerY + getWidth() / 2;
			int bottomborder = centerY - getWidth() / 2;

			bExpandToBottom = (mItemPosition > mSelectedPosition)
					? true
					: false;
			if (mSpaceY == 0)
				mSpaceY = (int) mRadiusX / mBufferedItemCount;
			mExpandableFinalY = centerY;
			mExpandY = centerY + (mItemPosition - mSelectedPosition) * mSpaceY;
			int gap = Math.abs(centerY - mExpandY);
			mExpandX = mExpandableFinalX = (int) (centerX - mPaddingX * gap
					/ (mRadiusY / mBufferedItemCount));

			scale = (float) (mScale - mScaleFactor * Math.pow(gap, mScaleExpr));
			mExpandWidth = (int) (origWidth * scale);
			mExpandHeight = (int) (origHeight * scale);
			calcluateExpandOffset_vert();
			bNeedDoExpand = true;
			if (bExpandToBottom)
				return (mExpandY + mExpandHeight / 2 + mExpandOffsetY <= topborder);
			else
				return (mExpandY - mExpandHeight / 2 + mExpandOffsetY >= bottomborder);
		}

		private void calcluateExpandOffset_horz() {
			if (enableOffsetX) {
				if (mSelectedPosition > mItemPosition)
					mExpandOffsetX = (int) ((-1) * (1.0 / mOffsetXFacter) * mExpandWidth);
				else if (mSelectedPosition < mItemPosition)
					mExpandOffsetX = (int) ((1.0 / mOffsetXFacter) * mExpandWidth);
				else
					mExpandOffsetX = 0;
			} else
				mExpandOffsetX = 0;
		}

		private void calcluateExpandOffset_vert() {
			if (enableOffsetY) {
				if (mSelectedPosition > mItemPosition)
					mExpandOffsetY = (int) ((-1) * (1.0 / mOffsetYFacter) * mExpandHeight);
				else if (mSelectedPosition < mItemPosition)
					mExpandOffsetY = (int) ((1.0 / mOffsetYFacter) * mExpandHeight);
				else
					mExpandOffsetY = 0;
			} else
				mExpandOffsetY = 0;
		}

		private boolean bNeedLayout = false;

		public FluidViewWrapper(View v, int itemPosition, int pos) {
			viewTag = v;
			mPos = pos;
			mItemPosition = itemPosition;

			// origWidth = mWidth = v.getMeasuredWidth();
			// origHeight = mHeight = v.getMeasuredHeight();
			origWidth = mWidth = getChildMeasureWidthEx(v);
			origHeight = mHeight = getChildMeasureHeightEx(v);
		}

		private void setX(int x) {
			mX = x;
		}

		private void setY(int y) {
			mY = y;
		}

		private void calcluateOffset_horz() {
			if (enableOffsetX) {
				if (mSelectedPosition > mItemPosition)
					offsetX = (int) ((-1) * (1.0 / mOffsetXFacter) * mWidth);
				else if (mSelectedPosition < mItemPosition)
					offsetX = (int) ((1.0 / mOffsetXFacter) * mWidth);
				else {
					int a = Math.abs(centerX - mX);
					float b = (float) a / (mSpaceX / 2);
					double c = ((1.0) / mOffsetXFacter) * mWidth;
					float d = (float) (b * c);
					if (mX > centerX)
						offsetX = (int) d;
					else
						offsetX = (int) d * (-1);
				}
			} else
				offsetX = 0;
		}

		private void calcluateOffset_vert() {
			if (enableOffsetY) {
				if (mSelectedPosition > mItemPosition) {
					// so ugly , need to improver later zhurui
					// offsetY = (int) ((-1) * (1.0 / mOffsetYFacter) *
					// origHeight) ;
					if (mOrientation == Configuration.ORIENTATION_PORTRAIT
							&& mSelectedPosition > mItemPosition + 2)
						offsetY = (int) ((-1) * (1.0 / 3.0f * origHeight));
					else if (mSelectedPosition > mItemPosition + 1) {
						if (mOrientation == Configuration.ORIENTATION_PORTRAIT)
							offsetY = (int) ((-1) * (1.0 / 3.5f * origHeight));
						else
							offsetY = (int) ((-1) * (1.0 / 4.0f * origHeight));
					} else
						offsetY = (int) ((-1) * (1.0 / 5.0f * origHeight));
				} else if (mSelectedPosition < mItemPosition)
					// offsetY = (int) ((1.0 / mOffsetYFacter) * origHeight);
					if (mOrientation == Configuration.ORIENTATION_PORTRAIT
							&& mSelectedPosition > mItemPosition + 2)
						offsetY = (int) ((1.0 / 3.0f * origHeight));
					else if (mSelectedPosition < mItemPosition - 1) {
						if (mOrientation == Configuration.ORIENTATION_PORTRAIT)
							offsetY = (int) ((1.0 / 3.5f * origHeight));
						else
							offsetY = (int) ((1.0 / 4.0f * origHeight));
					} else
						offsetY = (int) ((1.0 / 5.0f * origHeight));

				else {
					int a = Math.abs(centerY - mY);
					float b = (float) a / (mSpaceY / 2);
					double c = ((1.0) / mOffsetYFacter) * mHeight;
					float d = (float) (b * c);
					if (mY > centerY)
						offsetY = (int) d;
					else
						offsetY = (int) d * (-1);
				}
			} else
				offsetY = 0;
		}

		private boolean calculatePos_horz() {
			bNeedLayout = true;

			int gap = Math.abs(centerX - mX);
			/*
			 * mY = (int) (centerY - mPaddingY * gap / (mRadiusX /
			 * mBufferedItemCount));
			 */
			mY = centerY;

			// Math cost much time ,so we have to avoid all the data get
			// scaled
			scale = (float) (mScale - mScaleFactor * Math.pow(gap, mScaleExpr));
			mWidth = (int) (origWidth * scale);
			mHeight = (int) (origHeight * scale);

			if (Math.abs(centerX - mX) < mSpaceX / 2)// we have
			{
				if (mSelectedPosition != mItemPosition) {
					mSelectedPosition = mItemPosition;
					viewTag.setSelected(true);
					fireCenterChanged();
				}
			}
			calcluateOffset_horz();
			return bNeedLayout;
		}

		private boolean calculatePos_vert() {
			bNeedLayout = true;

			int gap = Math.abs(centerY - mY);
			mX = centerX;

			// Math cost much time ,so we have to avoid all the data get
			// scaled
			scale = (float) (mScale - mScaleFactor * Math.pow(gap, mScaleExpr));

			mWidth = (int) (origWidth * scale);
			mHeight = (int) (origHeight * scale);

			if (Math.abs(centerY - mY) < mSpaceY / 2)// we have
			{
				if (mSelectedPosition != mItemPosition) {
					mSelectedPosition = mItemPosition;
					viewTag.setSelected(true);
					fireCenterChanged();
				}
			}
			calcluateOffset_vert();
			return bNeedLayout;
		}

		private void scrollByX(int distance) {
			mX += distance;

			calculatePos_horz();
		}

		private void scrollByY(int distance) {
			mY += distance;
			calculatePos_vert();
		}
	}

	private void onUp() {
		if (mFlingRunnable.mScroller.isFinished()) {
			setSelectionToCenterChild();
		}
		dispatchUnpress();
	}

	private void onCancel() {
		onUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!bTouchable)
			return true;
		// Log.i("Fling","onTouchEvent *******************************");
		if (bInExpandAnimating)
			return true;
		if (!bExpanded) {
			expand();
			return true;
		}
		if (mAdapter == null || mAdapter.getCount() <= 0)
			return true;

		boolean retValue = mGestureDetector.onTouchEvent(event);

		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP) {
			// Helper method for lifted finger
			onUp();
		} else if (action == MotionEvent.ACTION_CANCEL) {
			onCancel();
		}

		return true;
	}

	private void detachLeft() {
		if (mFluidWrapperCount <= 0)
			return;

		int count = 0;
		int start = 0;
		FluidViewWrapper w = mWrapperList[0];
		int left = w.mX - w.mWidth / 2;
		while ((left < mGalleryDisplayLeft - mSpaceX /*
													 * || w.mItemPosition <
													 * mSelectedPosition - 3
													 */)
				&& mFluidWrapperCount > 1) {
			count++;
			if (count >= mFluidWrapperCount)
				break;
			w = mWrapperList[count];
			left = w.mX - w.mWidth / 2;
		}
		if (count <= 0)
			return;
		removeWrapperFromArray(start, count);

		/*
		 * for(int i = start ; i < start + count ; i ++) { View child =
		 * getChildAt(i); if(child instanceof ImageView) { ImageView v =
		 * (ImageView)child; BitmapDrawable bitmapDrawable =
		 * (BitmapDrawable)v.getDrawable(); Bitmap map =
		 * bitmapDrawable.getBitmap(); map.recycle(); } }
		 */
		detachViewsFromParent(start, count);

		mFirstPosition = mFirstPosition + count;
		if (mSelectedPosition < mFirstPosition) {
			mSelectedPosition = mFirstPosition;
			fireCenterChanged();
		}
	}

	private void fillRight() {
		FluidViewWrapper w = mWrapperList[mFluidWrapperCount - 1];
		int right = w.mX + w.mWidth / 2;
		int lastX = w.mX;
		int times = 0;
		while (right < mGalleryDisplayRight + mSpaceX
				&& w.mItemPosition < mAdapter.getCount() - 1) {
			times++;
			int itemPos = w.mItemPosition;
			w = makeAndAddView(itemPos + 1, mFluidWrapperCount,
					mFluidWrapperCount);

			w.setX(lastX + mSpaceX);

			lastX = w.mX;
			w.calculatePos_horz();
			right = w.mX + w.mWidth / 2;
		}
	}

	private void detachRight() {
		if (mFluidWrapperCount <= 0)
			return;
		int count = 0;
		int size = mFluidWrapperCount;
		int start = size;
		FluidViewWrapper w = mWrapperList[size - 1];
		int right = w.mX + w.mWidth / 2;

		while ((right > mGalleryDisplayRight + mSpaceX /*
														 * || w.mItemPosition >
														 * mSelectedPosition + 3
														 */)
				&& count < size - 1) {
			count++;
			start--;
			if (count >= size)
				break;
			w = mWrapperList[size - count - 1];
			right = w.mX + w.mWidth / 2;
		}
		if (count <= 0)
			return;

		removeWrapperFromArray(start, count);

		/*
		 * for(int i = start ; i < start + count ; i ++) { View child =
		 * getChildAt(i); if(child instanceof ImageView) { ImageView v =
		 * (ImageView)child; BitmapDrawable bitmapDrawable =
		 * (BitmapDrawable)v.getDrawable(); Bitmap map =
		 * bitmapDrawable.getBitmap(); map.recycle(); } }
		 */
		detachViewsFromParent(start, count);

		if (mSelectedPosition >= start + mFirstPosition) {
			// og.i("FLing","selected position ******** "+ mSelectedPosition +
			// "  "+ start);
			mSelectedPosition = start - 1 + mFirstPosition;
			fireCenterChanged();
		}

		if (mFirstPosition > mSelectedPosition)
			mFirstPosition = mSelectedPosition - mFluidWrapperCount + 1;
	}

	private void fillLeft() {
		FluidViewWrapper w = mWrapperList[0];
		int left = w.mX - w.mWidth / 2;
		int lastX = w.mX;
		while (left > mGalleryDisplayLeft - mSpaceX && w.mItemPosition > 0) {
			int itemPos = w.mItemPosition;
			w = makeAndAddView(itemPos - 1, 0, 0);
			w.setX(lastX - mSpaceX);
			w.calculatePos_horz();
			lastX = w.mX;
			left = w.mX - w.mWidth / 2;
		}
		mFirstPosition = w.mItemPosition;
	}

	private void detachTop() {
		if (mFluidWrapperCount <= 0)
			return;

		// Log.i("MusicCoverflow " , "Detach top ************* ");
		int count = 0;
		int start = 0;
		FluidViewWrapper w = mWrapperList[0];
		int top = w.mY - w.mHeight / 2;
		// special for RocketVCover flow
		while ((top < mGalleryDisplayTop - mSpaceY /*
													 * || w.mItemPosition <
													 * mSelectedPosition - 3
													 */)
				&& mFluidWrapperCount > 1) {
			count++;
			if (count >= mFluidWrapperCount)
				break;
			w = mWrapperList[count];
			top = w.mY - w.mHeight / 2;
		}
		if (count <= 0)
			return;
		removeWrapperFromArray(start, count);

		// special for Rocket Cover flow
		/*
		 * for(int i = start ; i < start + count ; i ++) { ImageView v =
		 * (ImageView)getChildAt(i); BitmapDrawable bitmapDrawable =
		 * (BitmapDrawable)v.getDrawable(); Bitmap map =
		 * bitmapDrawable.getBitmap(); map.recycle(); //Log.i("Cover Flow ",
		 * " Detach ******************* " + i); }
		 */
		detachViewsFromParent(start, count);

		mFirstPosition = mFirstPosition + count;
		if (mSelectedPosition < mFirstPosition) {
			mSelectedPosition = mFirstPosition;
			fireCenterChanged();
		}
	}

	private void fillBottom() {
		FluidViewWrapper w = mWrapperList[mFluidWrapperCount - 1];
		int bottom = w.mY + w.mHeight / 2;
		int lastY = w.mY;
		int times = 0;
		while (bottom < mGalleryDisplayBottom + mSpaceY
				&& w.mItemPosition < mAdapter.getCount() - 1) {
			times++;
			int itemPos = w.mItemPosition;
			w = makeAndAddView(itemPos + 1, mFluidWrapperCount,
					mFluidWrapperCount);

			w.setY(lastY + mSpaceY);

			lastY = w.mY;
			w.calculatePos_vert();
			bottom = w.mY + w.mHeight / 2;
		}
	}

	private void detachBottom() {
		if (mFluidWrapperCount <= 0)
			return;
		// Log.i("MusicCoverflow", "detach bottom ************************** ");

		int count = 0;
		int size = mFluidWrapperCount;
		int start = size;
		FluidViewWrapper w = mWrapperList[size - 1];
		int bottom = w.mY + w.mHeight / 2;

		while ((bottom > mGalleryDisplayBottom + mSpaceY /*
														 * || w.mItemPosition >
														 * mSelectedPosition + 3
														 */)
				&& count < size - 1) {
			count++;
			start--;
			if (count >= size)
				break;
			w = mWrapperList[size - count - 1];
			bottom = w.mY + w.mHeight / 2;
		}
		if (count <= 0)
			return;

		removeWrapperFromArray(start, count);

		/*
		 * for(int i = start ; i < start + count ; i ++) { ImageView v =
		 * (ImageView)getChildAt(i); BitmapDrawable bitmapDrawable =
		 * (BitmapDrawable)v.getDrawable(); Bitmap map =
		 * bitmapDrawable.getBitmap(); map.recycle(); //Log.i("Cover Flow ",
		 * " Detach ******************* " + i); }
		 */
		detachViewsFromParent(start, count);

		if (mSelectedPosition >= start + mFirstPosition) {
			// og.i("FLing","selected position ******** "+ mSelectedPosition +
			// "  "+ start);
			mSelectedPosition = start - 1 + mFirstPosition;
			fireCenterChanged();
		}

		if (mFirstPosition > mSelectedPosition)
			mFirstPosition = mSelectedPosition - mFluidWrapperCount + 1;
	}

	private void fillTop() {
		FluidViewWrapper w = mWrapperList[0];
		int top = w.mY - w.mHeight / 2;
		int lastY = w.mY;
		while (top > mGalleryDisplayTop - mSpaceY && w.mItemPosition > 0) {

			int itemPos = w.mItemPosition;
			w = makeAndAddView(itemPos - 1, 0, 0);

			w.setY(lastY - mSpaceY);
			w.calculatePos_vert();
			lastY = w.mY;
			top = w.mY - w.mHeight / 2;
		}
		mFirstPosition = w.mItemPosition;
	}

	private void calculateLayouts_horz(float mDistance) {
		boolean toLeft = (mDistance > 0) ? false : true;
		float limitedDistance = mDistance;
		if (toLeft) {
			FluidViewWrapper w = mWrapperList[mFluidWrapperCount - 1];
			if (w != null && w.mItemPosition == mAdapter.getCount() - 1) {
				if ((w.mX + mDistance) < centerX) {
					// Log.i("calculateLayouts_horz","mFlingRunnable.stop 2213....");
					mFlingRunnable.stop(false);
					limitedDistance = centerX - w.mX;
				} else if (w.mX < centerX) {
					// Log.i("calculateLayouts_horz","mFlingRunnable.stop 2217....");
					mFlingRunnable.stop(false);
					limitedDistance = centerX - w.mX;
				}
			}
		} else {
			FluidViewWrapper w = mWrapperList[0];
			if (w.mItemPosition == 0) {
				if (w.mX + mDistance > centerX) {
					// Log.i("calculateLayouts_horz","mFlingRunnable.stop 2226....");
					mFlingRunnable.stop(false);
					limitedDistance = centerX - w.mX;
				} else if (w.mX > centerX) {
					// Log.i("calculateLayouts_horz","mFlingRunnable.stop 2230....");
					mFlingRunnable.stop(false);
					limitedDistance = centerX - w.mX;
				}
			}
		}

		int size = mFluidWrapperCount;
		if (mDistance > 0) {
			for (int i = 0; i < size; i++) {
				FluidViewWrapper w = mWrapperList[i];
				w.scrollByX((int) limitedDistance);
			}
		} else {
			for (int i = size - 1; i >= 0; i--) {
				FluidViewWrapper w = mWrapperList[i];
				w.scrollByX((int) limitedDistance);
			}
		}

		if (toLeft) {
			fillRight();
			detachLeft();
		} else {
			fillLeft();
			detachRight();
		}
		layout_horz();
	}

	private void calculateLayouts_vert(float mDistance) {
		boolean toTop = (mDistance > 0) ? false : true;
		float limitedDistance = mDistance;
		if (toTop) {
			FluidViewWrapper w = mWrapperList[mFluidWrapperCount - 1];
			if (w.mItemPosition == mAdapter.getCount() - 1) {
				if ((w.mY + mDistance) < centerY) {
					// Log.i("calculateLayouts_horz","mFlingRunnable.stop 2267....");
					mFlingRunnable.stop(false);
					limitedDistance = centerY - w.mY;
				} else if (w.mY < centerY) {
					// Log.i("calculateLayouts_horz","mFlingRunnable.stop 2271....");
					mFlingRunnable.stop(false);
					limitedDistance = centerY - w.mY;
				}
			}
		} else {
			FluidViewWrapper w = mWrapperList[0];
			if (w.mItemPosition == 0) {
				if (w.mY + mDistance > centerY) {
					mFlingRunnable.stop(false);
					limitedDistance = centerY - w.mY;
				} else if (w.mY > centerY) {
					mFlingRunnable.stop(false);
					limitedDistance = centerY - w.mY;
				}
			}
		}

		int size = mFluidWrapperCount;
		if (mDistance > 0) {
			for (int i = 0; i < size; i++) {
				FluidViewWrapper w = mWrapperList[i];
				w.scrollByY((int) limitedDistance);
			}
		} else {
			for (int i = size - 1; i >= 0; i--) {
				FluidViewWrapper w = mWrapperList[i];
				w.scrollByY((int) limitedDistance);
			}
		}

		if (toTop) {
			fillBottom();
			detachTop();
		} else {
			fillTop();
			detachBottom();
		}
		layout_vert();
	}

	private boolean scrollToChild_horz(int listPosition) {
		if (mFluidWrapperCount <= 0)
			return false;
		FluidViewWrapper w = null;
		if (listPosition < mFluidWrapperCount)
			w = mWrapperList[listPosition];
		else
			w = mWrapperList[mFluidWrapperCount - 1];
		int distance = centerX - w.mX;
		// Log.i("scrollToChild_horz","scrollToChild_horz *******************:distance="+distance+",listPosition="+listPosition);

		// int maxDelta = getWidth() - getPaddingLeft() - getPaddingRight() / 4
		// + 1;
		// int mSec = (int) ((float) distance / maxDelta);
		// Log.i("scroll to child "," distance " +mSec);

		mFlingRunnable.startUsingDistance_horz(distance);
		return false;
	}

	private boolean scrollToChild_vert(int listPosition) {
		if (mFluidWrapperCount <= 0)
			return false;
		FluidViewWrapper w = null;
		if (listPosition < mFluidWrapperCount)
			w = mWrapperList[listPosition];
		else
			w = mWrapperList[mFluidWrapperCount - 1];
		int distance = centerY - w.mY;

		// int maxDelta = (getHeight() - getPaddingTop() - getPaddingBottom()) /
		// 4
		// + 1;
		// int mSec = (int) ((float) distance / maxDelta);
		// Log.i("scroll to child "," distance " +mSec);

		mFlingRunnable.startUsingDistance_vert(distance);
		return false;
	}

	private void setSelectionToCenterChild() {
		// Log.i("Fling","scroll to center child ******************* " +
		// mSelectedPosition + " ****** "+mFirstPosition);
		Log.i("setSelectionToCenterChild",
				"setSelectionToCenterChild *******************");
		if (mIsHorzCoverflow)
			scrollToChild_horz(mSelectedPosition - mFirstPosition);
		else
			scrollToChild_vert(mSelectedPosition - mFirstPosition);
	}

	private void dispatchUnpress() {
		for (int i = mFluidWrapperCount - 1; i >= 0; i--) {
			getChildAt(i).setPressed(false);
		}
		setPressed(false);
	}

	private void dispatchPress(View child) {
		if (child != null) {
			child.setPressed(true);
		}
		setPressed(true);
	}

	private Rect mTouchFrameEx;
	public int pointToPositionEx(int x, int y) {
		Rect frame = mTouchFrameEx;
		if (frame == null) {
			mTouchFrameEx = new Rect();
			frame = mTouchFrameEx;
		}

		final int iMid = mSelectedPosition - mFirstPosition;
		final int count = getChildCount();
		int i;
		for (i = iMid; i >= 0; i--) {
			View child = getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				child.getHitRect(frame);
				if (frame.contains(x, y)) {
					return mFirstPosition + i;
				}
			}
		}
		for (i = iMid + 1; i < count; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				child.getHitRect(frame);
				if (frame.contains(x, y)) {
					return mFirstPosition + i;
				}
			}
		}
		return INVALID_POSITION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeandroid.view.GestureDetector.OnGestureListener#onDown(android.view.
	 * MotionEvent)
	 */
	@Override
	public boolean onDown(MotionEvent e) {
		Log.i("onDown", "mFlingRunnable.stop 2412....");
		mFlingRunnable.stop(false);

		mDownTouchPosition = pointToPositionEx((int) e.getX(), (int) e.getY());

		if (mDownTouchPosition >= 0) {
			mDownTouchWrapper = mWrapperList[mDownTouchPosition
					- mFirstPosition];
			mDownTouchView = null;
			if (mDownTouchWrapper != null) {
				mDownTouchView = mDownTouchWrapper.viewTag;
				if (mDownTouchView != null)
					mDownTouchView.setPressed(true);
			} else {
				Log.i("#####onDown####", "mDownTouchPosition:"
						+ mDownTouchPosition);
			}
		}
		if (mDownTouchView == null)
			mDownTouchPosition = -1;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeandroid.view.GestureDetector.OnGestureListener#onFling(android.view.
	 * MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// Log.i("Fling","start Fling ******************************* 0");

		if (mFlingRunnable.bRunning)
			return true;
		if (mIsHorzCoverflow) {
			// Log.i("F","on fling *********************" +velocityX );
			if (velocityX > 0 && mSelectedPosition == 0) {
				// to right
				FluidViewWrapper w = mWrapperList[0];
				if (w.mX >= centerX)
					return true;
			} else if (velocityX < 0
					&& mSelectedPosition == mAdapter.getCount() - 1) {

				FluidViewWrapper w = mWrapperList[mFluidWrapperCount - 1];
				if (w.mX <= centerX)
					return true;
			}

			// let's make the velocity slower
			mFlingRunnable.startUsingVelocity_horz((int) -velocityX);
			// Log.i("Fling", "start Fling *******************************  "
			// + (-velocityX) + " " + (-velocityX / 2));
		} else {
			if (velocityY > 0 && mSelectedPosition == 0) {
				// to right
				FluidViewWrapper w = mWrapperList[0];
				if (w.mY >= centerY)
					return true;
			} else if (velocityY < 0
					&& mSelectedPosition == mAdapter.getCount() - 1) {
				FluidViewWrapper w = mWrapperList[mFluidWrapperCount - 1];
				if (w.mY <= centerY)
					return true;
			}
			// let's make the velocity slower
			mFlingRunnable.startUsingVelocity_vert((int) -velocityY);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.GestureDetector.OnGestureListener#onLongPress(android.view
	 * .MotionEvent)
	 */
	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (mFlingRunnable.bRunning)
			return true;
		if (mIsHorzCoverflow) {

			// getParent().requestDisallowInterceptTouchEvent(true);
			// Log.i("F","on scroll 1*********************" + distanceX +
			// "    "+mSelectedPosition);
			if (bOneSlotEachScroll) {
				if (distanceX < 0) // to bottom
					scrollToPosition_horz(mSelectedPosition - 1);
				else
					scrollToPosition_horz(mSelectedPosition + 1);
				return true;
			}

			if (distanceX < 0 && mSelectedPosition == 0) {
				// to right
				FluidViewWrapper w = mWrapperList[0];
				if (w.mX >= centerX)
					return true;
			} else if (distanceX > 0
					&& mSelectedPosition == mAdapter.getCount() - 1) {
				// Log.i("F","on scroll *********************");
				FluidViewWrapper w = mWrapperList[mFluidWrapperCount - 1];
				if (w.mX <= centerX)
					return true;
			}

			// FluidViewWrapper w = mWrapperList[mSelectedPosition];
			int iSpaceX = (int) (mRadiusX / mBufferedItemCount);
			if (Math.abs(distanceX) > iSpaceX) {
				if (distanceX > 0)
					distanceX = iSpaceX;// 负数向后移动
				else
					distanceX = -iSpaceX;
			}

			mDistance = (-1) * distanceX;

			calculateLayouts_horz(mDistance);
		} else {
			if (bOneSlotEachScroll) {
				if (distanceY < 0) // to bottom
					scrollToPosition_vert(mSelectedPosition - 1);
				else
					scrollToPosition_vert(mSelectedPosition + 1);
				return true;
			}

			if (distanceY < 0 && mSelectedPosition == 0) {
				// to right
				FluidViewWrapper w = mWrapperList[0];
				if (w.mY >= centerY)
					return true;
			} else if (distanceY > 0
					&& mSelectedPosition == mAdapter.getCount() - 1) {
				// Log.i("F","on scroll *********************");
				FluidViewWrapper w = mWrapperList[mFluidWrapperCount - 1];
				if (w.mY <= centerY)
					return true;
			}

			// FluidViewWrapper w = mWrapperList[mSelectedPosition];
			int iSpaceY = (int) (mRadiusY / mBufferedItemCount);
			if (Math.abs(distanceY) > iSpaceY) {
				if (distanceY > 0)
					distanceY = iSpaceY;// 负数向后移动
				else
					distanceY = -iSpaceY;
			}

			mDistance = (-1) * distanceY;

			calculateLayouts_vert(mDistance);
		}
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (mDownTouchPosition >= 0) {
			if (mShouldCallbackOnUnselectedItemClick
					|| mDownTouchPosition == mSelectedPosition - mFirstPosition) {

				performItemClick(mDownTouchView,
						mDownTouchWrapper.mItemPosition,
						mAdapter.getItemId(mDownTouchWrapper.mItemPosition));
			}
			return true;
		}
		return false;
	}

	public boolean isInScrollingOrAnimation() {
		boolean res = false;
		if (!mFlingRunnable.mScroller.isFinished())
			res = true;
		if (bInExpandAnimating)
			res = true;
		return res;
	}

	private class FlingRunnable implements Runnable {
		/**
		 * Tracks the decay of a fling scroll
		 */
		private ScrollerEx mScroller;
		private boolean bRunning = false;
		/**
		 * X value reported by mScroller on the previous fling
		 */
		private int mLastFlingX;
		private int mLastFlingY;

		public FlingRunnable() {
			// mScroller = new ScrollerEx(getContext(), new
			// DecelerateInterpolator());
			mScroller = new ScrollerEx(getContext());
		}

		private void startCommon() {
			// Remove any pending flings
			// Log.i("Fling","Start Common *****************");
			bRunning = true;
			removeCallbacks(this);
		}

		public void startUsingVelocity_horz(int initialVelocity) {
			if (initialVelocity == 0)
				return;
			startCommon();
			int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingX = initialX;
			int iOffsetPlusX = 0;
			FluidViewWrapper w = mWrapperList[mSelectedPosition
					- mFirstPosition];
			int iMax = 0;
			if (w != null) {
				iOffsetPlusX = w.mX - getCenterXOfGallery();
			}
			if (initialVelocity > 0) {
				iMax = mSpaceX
						* (getAdapter().getCount() - mSelectedPosition - 1)
						+ iOffsetPlusX;
			} else {
				iMax = mSpaceX * mSelectedPosition - iOffsetPlusX;
			}
			// mScroller.fling(initialX, 0, initialVelocity, 0, 0,
			// Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			mScroller.flingEx(0, 0, initialVelocity, 0, 0, iMax, 0,
					Integer.MAX_VALUE, mSpaceX, 0, iOffsetPlusX, 0);
			mLastFlingX = mScroller.getStartX();
			post(this);
		}

		public void startUsingVelocity_vert(int initialVelocity) {
			if (initialVelocity == 0)
				return;
			startCommon();
			int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingY = initialY;

			int iOffsetPlusX = 0, iOffsetPlusY = 0;
			FluidViewWrapper w = mWrapperList[mSelectedPosition
					- mFirstPosition];
			int iMax = 0;
			if (w != null) {
				iOffsetPlusY = w.mY - getCenterYOfGallery();
			}
			if (initialVelocity > 0) {
				iMax = mSpaceY
						* (getAdapter().getCount() - mSelectedPosition - 1)
						+ iOffsetPlusY;
			} else {
				iMax = mSpaceY * mSelectedPosition - iOffsetPlusY;
			}
			// mScroller.fling(0, initialY, 0, initialVelocity, 0,
			// Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			mScroller.flingEx(0, 0, 0, initialVelocity, 0, 0, 0, iMax, 0,
					mSpaceY, 0, iOffsetPlusY);
			mLastFlingY = mScroller.getStartY();
			post(this);
		}

		public void startUsingDistance_horz(int distance, int mSec) {
			if (distance == 0)
				return;
			startCommon();
			mLastFlingX = 0;
			// mScroller.startScroll(0, 0, -distance, 0, mSec);
			mScroller.startScrollEx(0, 0, -distance, 0, mSec, mSpaceX, 0);
			post(this);
		}

		public void startUsingDistance_vert(int distance, int mSec) {
			if (distance == 0)
				return;
			startCommon();
			mLastFlingY = 0;
			// mScroller.startScroll(0, 0, 0, -distance, mSec);
			mScroller.startScrollEx(0, 0, 0, -distance, mSec, 0, mSpaceY);
			post(this);
		}

		public void startUsingDistance_horz(int distance) {
			if (distance == 0)
				return;
			startCommon();
			mLastFlingX = 0;
			// mScroller.startScroll(0, 0, -distance, 0, autoScrollDuration);
			mScroller.startScrollEx(0, 0, -distance, 0, autoScrollDuration,
					mSpaceX, 0);
			post(this);
		}

		public void startUsingDistance_vert(int distance) {
			if (distance == 0)
				return;
			startCommon();
			mLastFlingY = 0;
			// mScroller.startScroll(0, 0, 0 , -distance, autoScrollDuration);
			mScroller.startScrollEx(0, 0, -distance, 0, autoScrollDuration, 0,
					mSpaceY);
			post(this);
		}

		public void stop(boolean scrollIntoSlots) {
			removeCallbacks(this);
			// Log.i("CoverFlow","stop endFling 2678....");
			endFling(scrollIntoSlots);
		}

		private void endFling(boolean scrollIntoSlots) {
			/*
			 * Force the scroller's status to finished (without setting its
			 * position to the end)
			 */
			// Log.i("Fling","Start Common *****************");
			bRunning = false;
			mScroller.forceFinished(true);
			// Log.i("Fling","end fling set Selection to center child *******************");
			if (scrollIntoSlots)
				setSelectionToCenterChild();
		}

		@Override
		public void run() {
			if (mFluidWrapperCount == 0) {
				endFling(true);
				return;
			}

			mShouldStopFling = false;

			if (mIsHorzCoverflow) {
				final ScrollerEx scroller = mScroller;
				// boolean more = scroller.computeScrollOffset();
				boolean more = scroller.computeScrollOffsetEx();
				final int x = scroller.getCurrX();

				// Flip sign to convert finger direction to list items direction
				// (e.g. finger moving down means list is moving towards the
				// top)
				int delta = mLastFlingX - x;
				// delta = Math.min(delta,(getWidth() - getPaddingLeft() -
				// getPaddingRight())/4 + 1);
				if (Math.abs(delta) > mSpaceX) {
					delta = delta > 0 ? mSpaceX : -mSpaceX;
				}
				if (delta != 0)
					calculateLayouts_horz(delta);
				// Log.i("scroll ", " distance *********** " + delta +
				// " if more " +
				// more);
				// Log.i("CoverFlow","run:delta="+delta+",more="+more+",curx="+x+",mLastFlingX="+mLastFlingX+",mShouldStopFling="+mShouldStopFling);
				if (more && !mShouldStopFling) {
					mLastFlingX = x;
					post(this);
					// postDelayed(this,50);
				} else {
					// Log.i("Fling--run-horz","Fling--run-horz:Finished SCroll");
					endFling(true);
				}
			} else {
				final ScrollerEx scroller = mScroller;
				// boolean more = scroller.computeScrollOffset();
				boolean more = scroller.computeScrollOffsetEx();
				final int y = scroller.getCurrY();

				// Flip sign to convert finger direction to list items direction
				// (e.g. finger moving down means list is moving towards the
				// top)
				int delta = mLastFlingY - y;
				if (Math.abs(delta) > mSpaceY) {
					delta = delta > 0 ? mSpaceY : -mSpaceY;
				}
				// delta = Math.min(delta,(getWidth() - getPaddingLeft() -
				// getPaddingRight())/4 + 1);
				if (delta != 0)
					calculateLayouts_vert(delta);
				// Log.i("scroll ", " distance *********** " + delta +
				// " if more " +
				// more);
				if (more && !mShouldStopFling) {
					mLastFlingY = y;
					post(this);
					// postDelayed(this,50);
				} else {
					// Log.i("Fling","Finished SCroll");
					endFling(true);
				}
			}
		}
	}

	@Override
	void layout(int delta, boolean animate) {

	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// buildOffsetCache();
		mGestureDetector = new GestureDetector(this);
		mGestureDetector.setIsLongpressEnabled(true);
		setChildrenDrawingOrderEnabled(true);
		setStaticTransformationsEnabled(true);
	}

	public class ScrollerEx {
		private int mMode;

		private int mXPlus;
		private int mYPlus;
		private float mTotalDistance;
		private float mCurrentPos;

		private int mStartX;
		private int mStartY;
		private int mFinalX;
		private int mFinalY;

		private int mMinX;
		private int mMaxX;
		private int mMinY;
		private int mMaxY;

		private int mCurrX;
		private int mCurrY;
		private long mStartTime;
		private int mDuration;
		private float mDurationReciprocal;
		private float mDeltaX;
		private float mDeltaY;
		private float mViscousFluidScale;
		private float mViscousFluidNormalize;
		private boolean mFinished;
		private Interpolator mInterpolator;

		private float mCoeffX = 0.0f;
		private float mCoeffY = 1.0f;
		private float mVelocity;

		private static final int DEFAULT_DURATION = 250;
		private static final int SCROLL_MODE = 0;
		private static final int FLING_MODE = 1;

		private final float mDeceleration;

		/**
		 * Create a Scroller with the default duration and interpolator.
		 */
		public ScrollerEx(Context context) {
			this(context, null);
		}

		/**
		 * Create a Scroller with the specified interpolator. If the
		 * interpolator is null, the default (viscous) interpolator will be
		 * used.
		 */
		public ScrollerEx(Context context, Interpolator interpolator) {
			mFinished = true;
			mInterpolator = interpolator;
			float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
			mDeceleration = SensorManager.GRAVITY_EARTH // g (m/s^2)
					* 39.37f // inch/meter
					* ppi // pixels per inch
					* ViewConfiguration.getScrollFriction();
		}

		/**
		 * 
		 * Returns whether the scroller has finished scrolling.
		 * 
		 * @return True if the scroller has finished scrolling, false otherwise.
		 */
		public final boolean isFinished() {
			return mFinished;
		}

		/**
		 * Force the finished field to a particular value.
		 * 
		 * @param finished
		 *            The new finished value.
		 */
		public final void forceFinished(boolean finished) {
			mFinished = finished;
		}

		/**
		 * Returns how long the scroll event will take, in milliseconds.
		 * 
		 * @return The duration of the scroll in milliseconds.
		 */
		public final int getDuration() {
			return mDuration;
		}

		/**
		 * Returns the current X offset in the scroll.
		 * 
		 * @return The new X offset as an absolute distance from the origin.
		 */
		public final int getCurrX() {
			return mCurrX;
		}

		/**
		 * Returns the current Y offset in the scroll.
		 * 
		 * @return The new Y offset as an absolute distance from the origin.
		 */
		public final int getCurrY() {
			return mCurrY;
		}

		/**
		 * @hide Returns the current velocity.
		 * 
		 * @return The original velocity less the deceleration. Result may be
		 *         negative.
		 */
		public float getCurrVelocity() {
			return mVelocity - mDeceleration * timePassed() / 2000.0f;
		}

		/**
		 * Returns the start X offset in the scroll.
		 * 
		 * @return The start X offset as an absolute distance from the origin.
		 */
		public final int getStartX() {
			return mStartX;
		}

		/**
		 * Returns the start Y offset in the scroll.
		 * 
		 * @return The start Y offset as an absolute distance from the origin.
		 */
		public final int getStartY() {
			return mStartY;
		}

		/**
		 * Returns where the scroll will end. Valid only for "fling" scrolls.
		 * 
		 * @return The final X offset as an absolute distance from the origin.
		 */
		public final int getFinalX() {
			return mFinalX;
		}

		/**
		 * Returns where the scroll will end. Valid only for "fling" scrolls.
		 * 
		 * @return The final Y offset as an absolute distance from the origin.
		 */
		public final int getFinalY() {
			return mFinalY;
		}

		private void initPlusParams(int xPlus, int yPlus, int totalDistance) {
			mCurrX = mStartX;
			mCurrY = mStartY;
			mTotalDistance = Math.abs(totalDistance);
			mCurrentPos = 0;

			if (mFinalX >= mStartX)
				mXPlus = Math.abs(xPlus) / 2;
			else
				mXPlus = -Math.abs(xPlus) / 2;

			if (mFinalY >= mStartY)
				mYPlus = Math.abs(yPlus) / 2;
			else
				mYPlus = -Math.abs(yPlus) / 2;
		}

		public float getInterpolation(float input) {
			return (1.0f - (1.0f - input) * (1.0f - input));
		}

		public boolean computeScrollOffsetEx() {
			if (mFinished) {
				return false;
			}

			float fProgress, fX, fY;
			float fTemp = 0;
			fProgress = mCurrentPos / mTotalDistance;
			fProgress = 1 - getInterpolation(fProgress);
			if (mXPlus != 0) {
				fX = mXPlus * fProgress;
				fY = 0;
				fTemp = Math.abs(fX);
				if (fTemp < 2.0f) {
					fTemp = 2.0f;
					fX = mXPlus > 0 ? 2.0f : -2.0f;
				}
			} else {
				fX = 0;
				fY = mYPlus * fProgress;
				fTemp = Math.abs(fY);
				if (fTemp < 2.0f) {
					fTemp = 2.0f;
					fY = mYPlus > 0 ? 2.0f : -2.0f;
				}
			}

			mCurrentPos += fTemp;

			mCurrX += fX;
			mCurrY += fY;

			// Log.i("ScrollerEx","computeScrollOffsetEx:cur(x,y)=("+mCurrX+","+mCurrY+",end(x,y)=("+mFinalX+","+mFinalY+"),fProgress="+fProgress);
			if (mXPlus >= 0 && mCurrX >= mFinalX && mYPlus >= 0
					&& mCurrY >= mFinalY || mXPlus <= 0 && mCurrX <= mFinalX
					&& mYPlus <= 0 && mCurrY <= mFinalY) {
				mCurrX = mFinalX;
				mCurrY = mFinalY;
				mFinished = true;
			}
			return true;
		}

		public void startScrollEx(int startX, int startY, int dx, int dy,
				int xPlus, int yPlus) {
			startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
			initPlusParams(xPlus, yPlus, dx != 0 ? dx * DEFAULT_DURATION : dy
					* DEFAULT_DURATION);
		}

		public void startScrollEx(int startX, int startY, int dx, int dy,
				int duration, int xPlus, int yPlus) {
			startScroll(startX, startY, dx, dy, duration);
			initPlusParams(xPlus, yPlus, dx != 0 ? dx * duration : dy
					* duration);
		}

		public void flingEx(int startX, int startY, int velocityX,
				int velocityY, int minX, int maxX, int minY, int maxY,
				int xPlus, int yPlus, int iOffsetPlusX, int iOffsetPlusY) {
			mMode = FLING_MODE;
			mFinished = false;

			velocityX /= 3;
			velocityY /= 3;

			float velocity = (float) Math.hypot(velocityX, velocityY);

			mVelocity = velocity;
			mDuration = (int) (1000 * velocity / mDeceleration); // Duration is
																	// in
																	// milliseconds
			mStartTime = AnimationUtils.currentAnimationTimeMillis();

			mCoeffX = velocity == 0 ? 1.0f : velocityX / velocity;
			mCoeffY = velocity == 0 ? 1.0f : velocityY / velocity;

			int totalDistance = (int) ((velocity * velocity) / (2 * mDeceleration));

			// mMinX = 0;
			// mMaxX = Math.round(totalDistance * mCoeffX);
			// mMinY = 0;
			// mMaxY = Math.round(totalDistance * mCoeffY);

			if (velocityX < 0 || velocityY < 0) {
				if (xPlus != 0) {
					mStartX = -(Math.round(totalDistance * mCoeffX) + xPlus - 1)
							/ xPlus * xPlus;
					mStartX -= iOffsetPlusX;
				} else
					mStartX = 0;
				if (yPlus != 0) {
					mStartY = -(Math.round(totalDistance * mCoeffY) + yPlus - 1)
							/ yPlus * yPlus;
					mStartY -= iOffsetPlusY;
				} else
					mStartY = 0;

				if (mStartX > maxX)
					mStartX = maxX;
				if (mStartY > maxY)
					mStartY = maxY;
				mFinalX = 0;
				mFinalY = 0;
			} else {
				mStartX = 0;
				mStartY = 0;
				if (xPlus != 0) {
					mFinalX = ((Math.round(totalDistance * mCoeffX) + xPlus - 1) / xPlus)
							* xPlus;
					mFinalX += iOffsetPlusX;
				} else
					mFinalX = 0;
				if (yPlus != 0) {
					mFinalY = ((Math.round(totalDistance * mCoeffY) + yPlus - 1) / yPlus)
							* yPlus;
					mFinalY += iOffsetPlusY;
				} else
					mFinalY = 0;

				if (mFinalX > maxX)
					mFinalX = maxX;
				if (mFinalY > maxY)
					mFinalY = maxY;
			}

			Log.i("ScrollerEx", "fling:<velocityX,velocityY>=<" + velocityX
					+ "," + velocityY + ">,<mFinalX,mFinalY>=<" + mFinalX + ","
					+ mFinalY + ">,<mStartX,mStartY>=<" + mStartX + ","
					+ mStartY + ">");
			// fling(startX,startY,velocityX,velocityY,minX,maxX,minY,minY);
			// int totalDistance = (int) ((mVelocity * mVelocity) / (2 *
			// mDeceleration));
			initPlusParams(
					xPlus,
					yPlus,
					mCoeffX != 0 ? Math.round(totalDistance * mCoeffX) : Math
							.round(totalDistance * mCoeffY));
		}
		/**
		 * Call this when you want to know the new location. If it returns true,
		 * the animation is not yet finished. loc will be altered to provide the
		 * new location.
		 */
		public boolean computeScrollOffset() {
			if (mFinished) {
				return false;
			}

			int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);

			if (timePassed < mDuration) {
				switch (mMode) {
					case SCROLL_MODE :
						float x = timePassed * mDurationReciprocal;

						if (mInterpolator == null)
							x = viscousFluid(x);
						else
							x = mInterpolator.getInterpolation(x);

						mCurrX = mStartX + Math.round(x * mDeltaX);
						mCurrY = mStartY + Math.round(x * mDeltaY);
						break;
					case FLING_MODE :
						float timePassedSeconds = timePassed / 1000.0f;
						float distance = (mVelocity * timePassedSeconds)
								- (mDeceleration * timePassedSeconds
										* timePassedSeconds / 2.0f);

						mCurrX = mStartX + Math.round(distance * mCoeffX);
						// Pin to mMinX <= mCurrX <= mMaxX
						mCurrX = Math.min(mCurrX, mMaxX);
						mCurrX = Math.max(mCurrX, mMinX);

						mCurrY = mStartY + Math.round(distance * mCoeffY);
						// Pin to mMinY <= mCurrY <= mMaxY
						mCurrY = Math.min(mCurrY, mMaxY);
						mCurrY = Math.max(mCurrY, mMinY);

						break;
				}
			} else {
				mCurrX = mFinalX;
				mCurrY = mFinalY;
				mFinished = true;
			}
			return true;
		}

		/**
		 * Start scrolling by providing a starting point and the distance to
		 * travel. The scroll will use the default value of 250 milliseconds for
		 * thepublic void startScroll(int startX, int startY, int dx, int dy) {
		 * startScroll(startX, startY, dx, dy, DEFAULT_DURATION); } duration.
		 * 
		 * @param startX
		 *            Starting horizontal scroll offset in pixels. Positive
		 *            numbers will scroll the content to the left.
		 * @param startY
		 *            Starting vertical scroll offset in pixels. Positive
		 *            numbers will scroll the content up.
		 * @param dx
		 *            Horizontal distance to travel. Positive numbers will
		 *            scroll the content to the left.
		 * @param dy
		 *            Vertical distance to travel. Positive numbers will scroll
		 *            the content up.
		 */
		public void startScroll(int startX, int startY, int dx, int dy) {
			startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
		}

		/**
		 * Start scrolling by providing a starting point and the distance to
		 * travel.
		 * 
		 * @param startX
		 *            Starting horizontal scroll offset in pixels. Positive
		 *            numbers will scroll the content to the left.
		 * @param startY
		 *            Starting vertical scroll offset in pixels. Positive
		 *            numbers will scroll the content up.
		 * @param dx
		 *            Horizontal distance to travel. Positive numbers will
		 *            scroll the content to the left.
		 * @param dy
		 *            Vertical distance to travel. Positive numbers will scroll
		 *            the content up.
		 * @param duration
		 *            Duration of the scroll in milliseconds.
		 */
		public void startScroll(int startX, int startY, int dx, int dy,
				int duration) {
			mMode = SCROLL_MODE;
			mFinished = false;
			mDuration = duration;
			mStartTime = AnimationUtils.currentAnimationTimeMillis();
			mStartX = startX;
			mStartY = startY;
			mFinalX = startX + dx;
			mFinalY = startY + dy;
			mDeltaX = dx;
			mDeltaY = dy;
			mDurationReciprocal = 1.0f / mDuration;
			// This controls the viscous fluid effect (how much of it)
			mViscousFluidScale = 8.0f;
			// must be set to 1.0 (used in viscousFluid())
			mViscousFluidNormalize = 1.0f;
			mViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
		}

		/**
		 * Start scrolling based on a fling gesture. The distance travelled will
		 * depend on the initial velocity of the fling.
		 * 
		 * @param startX
		 *            Starting point of the scroll (X)
		 * @param startY
		 *            Starting point of the scroll (Y)
		 * @param velocityX
		 *            Initial velocity of the fling (X) measured in pixels per
		 *            second.
		 * @param velocityY
		 *            Initial velocity of the fling (Y) measured in pixels per
		 *            second
		 * @param minX
		 *            Minimum X value. The scroller will not scroll past this
		 *            point.
		 * @param maxX
		 *            Maximum X value. The scroller will not scroll past this
		 *            point.
		 * @param minY
		 *            Minimum Y value. The scroller will not scroll past this
		 *            point.
		 * @param maxY
		 *            Maximum Y value. The scroller will not scroll past this
		 *            point.
		 */
		public void fling(int startX, int startY, int velocityX, int velocityY,
				int minX, int maxX, int minY, int maxY) {

			mMode = FLING_MODE;
			mFinished = false;

			float velocity = (float) Math.hypot(velocityX, velocityY);

			mVelocity = velocity;
			mDuration = (int) (1000 * velocity / mDeceleration); // Duration is
																	// in
																	// milliseconds
			mStartTime = AnimationUtils.currentAnimationTimeMillis();
			mStartX = startX;
			mStartY = startY;

			mCoeffX = velocity == 0 ? 1.0f : velocityX / velocity;
			mCoeffY = velocity == 0 ? 1.0f : velocityY / velocity;

			int totalDistance = (int) ((velocity * velocity) / (2 * mDeceleration));

			mMinX = minX;
			mMaxX = maxX;
			mMinY = minY;
			mMaxY = maxY;

			mFinalX = startX + Math.round(totalDistance * mCoeffX);
			// Pin to mMinX <= mFinalX <= mMaxX
			mFinalX = Math.min(mFinalX, mMaxX);
			mFinalX = Math.max(mFinalX, mMinX);

			mFinalY = startY + Math.round(totalDistance * mCoeffY);
			// Pin to mMinY <= mFinalY <= mMaxY
			mFinalY = Math.min(mFinalY, mMaxY);
			mFinalY = Math.max(mFinalY, mMinY);

			Log.i("ScrollerEx", "fling:<velocityX,velocityY>=<" + velocityX
					+ "," + velocityY + ">,<mFinalX,mFinalY>=<" + mFinalX + ","
					+ mFinalY + ">,<mStartX,mStartY>=<" + mStartX + ","
					+ mStartY + ">");
		}

		private float viscousFluid(float x) {
			x *= mViscousFluidScale;
			if (x < 1.0f) {
				x -= (1.0f - (float) Math.exp(-x));
			} else {
				float start = 0.36787944117f; // 1/e == exp(-1)
				x = 1.0f - (float) Math.exp(1.0f - x);
				x = start + x * (1.0f - start);
			}
			x *= mViscousFluidNormalize;
			return x;
		}

		/**
		 * Stops the animation. Contrary to {@link #forceFinished(boolean)},
		 * aborting the animating cause the scroller to move to the final x and
		 * y position
		 * 
		 * @see #forceFinished(boolean)
		 */
		public void abortAnimation() {
			mCurrX = mFinalX;
			mCurrY = mFinalY;
			mFinished = true;
		}

		/**
		 * Extend the scroll animation. This allows a running animation to
		 * scroll further and longer, when used with {@link #setFinalX(int)} or
		 * {@link #setFinalY(int)}.
		 * 
		 * @param extend
		 *            Additional time to scroll in milliseconds.
		 * @see #setFinalX(int)
		 * @see #setFinalY(int)
		 */
		public void extendDuration(int extend) {
			int passed = timePassed();
			mDuration = passed + extend;
			mDurationReciprocal = 1.0f / mDuration;
			mFinished = false;
		}

		/**
		 * Returns the time elapsed since the beginning of the scrolling.
		 * 
		 * @return The elapsed time in milliseconds.
		 */
		public int timePassed() {
			return (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
		}

		/**
		 * Sets the final position (X) for this scroller.
		 * 
		 * @param newX
		 *            The new X offset as an absolute distance from the origin.
		 * @see #extendDuration(int)
		 * @see #setFinalY(int)
		 */
		public void setFinalX(int newX) {
			mFinalX = newX;
			mDeltaX = mFinalX - mStartX;
			mFinished = false;
		}

		/**
		 * Sets the final position (Y) for this scroller.
		 * 
		 * @param newY
		 *            The new Y offset as an absolute distance from the origin.
		 * @see #extendDuration(int)
		 * @see #setFinalX(int)
		 */
		public void setFinalY(int newY) {
			mFinalY = newY;
			mDeltaY = mFinalY - mStartY;
			mFinished = false;
		}
	}
}
