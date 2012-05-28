/**
 * =====================================================================
 *
 * @file  GalleryHW.java
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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;

@SuppressWarnings("unused")
public class GalleryHW extends AbsSpinnerEx
		implements
			GestureDetector.OnGestureListener {

	private int mChildViewHeight = -1;
	private int mChildViewWidth = -1;
	private static final String TAG = "GalleryHW";

	private static final boolean localLOGV = false;

	/**
	 * Duration in milliseconds from the start of a scroll during which we're
	 * unsure whether the user is scrolling or flinging.
	 */
	private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;

	/**
	 * Horizontal spacing between items.
	 */
	private int mSpacing = 0;

	/**
	 * How long the transition animation should run when a child view changes
	 * position, measured in milliseconds.
	 */
	private int mAnimationDuration = 400;

	/**
	 * The alpha of items that are not selected.
	 */
	private float mUnselectedAlpha;

	/**
	 * Left/right/top/bottom most edge of a child seen so far during layout.
	 */
	private int mLeftMost;
	private int mRightMost;
	private int mTopMost;
	private int mBottomMost;

	/**
	 * 是否水平滚动
	 */
	private boolean mIsScrollHoriz;
	/**
	 * 每列图片数目(水平滚动时)，每行图片数目（垂直滚动时）
	 */
	private int mColumnCount;
	/**
	 * 是否变成中间大两边小的效果(1),缺省为0，表示不
	 */
	private int mCustomDisplayMode;
	private int mColumnSpace;
	private int mColumnSpaceIndeed = 0;

	private int mGravity;

	/**
	 * Helper for detecting touch gestures.
	 */
	private GestureDetector mGestureDetector;

	/**
	 * The position of the item that received the user's down touch.
	 */
	private int mDownTouchPosition;

	/**
	 * The view of the item that received the user's down touch.
	 */
	private View mDownTouchView;

	/**
	 * Executes the delta scrolls from a fling or scroll movement.
	 */
	private FlingRunnable mFlingRunnable = new FlingRunnable();

	/**
	 * Sets mSuppressSelectionChanged = false. This is used to set it to false
	 * in the future. It will also trigger a selection changed.
	 */
	private Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {
		@Override
		public void run() {
			mSuppressSelectionChanged = false;
			selectionChanged();
		}
	};

	/**
	 * When fling runnable runs, it resets this to false. Any method along the
	 * path until the end of its run() can set this to true to abort any
	 * remaining fling. For example, if we've reached either the leftmost or
	 * rightmost item, we will set this to true.
	 */
	private boolean mShouldStopFling;

	/**
	 * The currently selected item's child.
	 */
	private View mSelectedChild;

	/**
	 * Whether to continuously callback on the item selected listener during a
	 * fling.
	 */
	private boolean mShouldCallbackDuringFling = true;

	/**
	 * Whether to callback when an item that is not selected is clicked.
	 */
	private boolean mShouldCallbackOnUnselectedItemClick = true;

	/**
	 * If true, do not callback to item selected listener.
	 */
	private boolean mSuppressSelectionChanged;

	/**
	 * If true, we have received the "invoke" (center or enter buttons) key
	 * down. This is checked before we action on the "invoke" key up, and is
	 * subsequently cleared.
	 */
	private boolean mReceivedInvokeKeyDown;

	private AdapterContextMenuInfo mContextMenuInfo;

	/**
	 * If true, this onScroll is the first for this user's drag (remember, a
	 * drag sends many onScrolls).
	 */
	private boolean mIsFirstScroll;

	public GalleryHW(Context context) {
		this(context, null);
	}

	public GalleryHW(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GalleryHW(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mIsScrollHoriz = true; // 垂直
		mColumnCount = 1;
		mCustomDisplayMode = 0;
		mColumnSpace = 0;

		mGestureDetector = new GestureDetector(context, this);
		mGestureDetector.setIsLongpressEnabled(true);

		/*
		 * TypedArray a = context.obtainStyledAttributes( attrs,
		 * com.android.internal.R.styleable.Gallery, defStyle, 0);
		 * 
		 * int index =
		 * a.getInt(com.android.internal.R.styleable.Gallery_gravity, -1); if
		 * (index >= 0) { setGravity(index); }
		 * 
		 * int animationDuration =
		 * a.getInt(com.android.internal.R.styleable.Gallery_animationDuration,
		 * -1); if (animationDuration > 0) {
		 * setAnimationDuration(animationDuration); }
		 * 
		 * int spacing =
		 * a.getDimensionPixelOffset(com.android.internal.R.styleable
		 * .Gallery_spacing, 0); setSpacing(spacing);
		 * 
		 * float unselectedAlpha = a.getFloat(
		 * com.android.internal.R.styleable.Gallery_unselectedAlpha, 0.5f);
		 * setUnselectedAlpha(unselectedAlpha);
		 * 
		 * a.recycle();
		 * 
		 * // We draw the selected item last (because otherwise the item to the
		 * // right overlaps it) mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;
		 * 
		 * mGroupFlags |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;
		 */
	}

	/**
	 * Whether or not to callback on any## while the items are being flinged. If
	 * false, only the final selected item will cause the callback. If true, all
	 * items between the first and the final will cause callbacks.
	 * 
	 * @param shouldCallback
	 *            Whether or not to callback on the listener while the items are
	 *            being flinged.
	 */
	public void setCallbackDuringFling(boolean shouldCallback) {
		mShouldCallbackDuringFling = shouldCallback;
	}

	/**
	 * Whether or not to callback when an item that is not selected is clicked.
	 * If false, the item will become selected (and re-centered). If true, the
	 * ##will get the callback.
	 * 
	 * @param shouldCallback
	 *            Whether or not to callback on the listener when a item that is
	 *            not selected is clicked.
	 * @hide
	 */
	public void setCallbackOnUnselectedItemClick(boolean shouldCallback) {
		mShouldCallbackOnUnselectedItemClick = shouldCallback;
	}

	/**
	 * Sets how long the transition animation should run when a child view
	 * changes position. Only relevant if animation is turned on.
	 * 
	 * @param animationDurationMillis
	 *            The duration of the transition, in milliseconds.
	 * 
	 * @attr ref android.R.styleable#Gallery_animationDuration
	 */
	public void setAnimationDuration(int animationDurationMillis) {
		mAnimationDuration = animationDurationMillis;
	}

	/**
	 * Sets the spacing between items in a Gallery
	 * 
	 * @param spacing
	 *            The spacing in pixels between items in the Gallery
	 * 
	 * @attr ref android.R.styleable#Gallery_spacing
	 */
	public void setSpacing(int spacing) {
		mSpacing = spacing;
	}

	public void setColumnSpacing(int spacing) {
		mColumnSpace = spacing;
	}

	public void setScrollHorize(boolean bScrollHoriz) {
		mIsScrollHoriz = bScrollHoriz;
		mRecycler.clear();
	}

	public void setColumnCount(int iColumnCount) {
		mColumnCount = iColumnCount;
		mRecycler.clear();
	}
	/**
	 * Sets the alpha of items that are not selected in the Gallery.
	 * 
	 * @param unselectedAlpha
	 *            the alpha for the items that are not selected.
	 * 
	 * @attr ref android.R.styleable#Gallery_unselectedAlpha
	 */
	public void setUnselectedAlpha(float unselectedAlpha) {
		mUnselectedAlpha = unselectedAlpha;
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {

		t.clear();
		t.setAlpha(child == mSelectedChild ? 1.0f : mUnselectedAlpha);

		return true;
	}

	@Override
	protected int computeHorizontalScrollExtent() {
		// Only 1 item is considered to be selected
		return 1;
	}

	@Override
	protected int computeHorizontalScrollOffset() {
		// Current scroll position is the same as the selected position
		return mSelectedPosition;
	}

	@Override
	protected int computeHorizontalScrollRange() {
		// Scroll range is the same as the item count
		return mItemCount;
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(
			ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		/*
		 * Gallery expects Gallery.LayoutParams.
		 */
		return new GalleryHW.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		/*
		 * Remember that we are in layout to prevent more layout request from
		 * being generated.
		 */
		mInLayout = true;
		if (mIsScrollHoriz)
			layoutX(0, false);
		else
			layoutY(0, false);
		mInLayout = false;
	}

	@Override
	int getChildHeight(View child) {
		return child.getMeasuredHeight();
	}

	/**
	 * Tracks a motion scroll. In reality, this is used to do just about any
	 * movement to items (touch scroll, arrow-key scroll, set an item as
	 * selected).
	 * 
	 * @param deltaX
	 *            Change in X from the previous event.
	 */
	void trackMotionScrollX(int deltaXY) {
		if (getChildCount() == 0) {
			return;
		}

		boolean toLeft = deltaXY < 0;
		int limitedDeltaX = getLimitedMotionScrollAmountX(toLeft, deltaXY);
		if (limitedDeltaX != deltaXY) {
			// The above call returned a limited amount, so stop any
			// scrolls/flings
			mFlingRunnable.endFling(false);
			onFinishedMovement();
		}
		offsetChildrenLeftAndRight(limitedDeltaX);
		detachOffScreenChildrenX(toLeft);
		if (toLeft) {
			// If moved left, there will be empty space on the right
			fillToGalleryRight();
		} else {
			// Similarly, empty space on the left
			fillToGalleryLeft();
		}
		// Clear unused views
		mRecycler.clear();
		setSelectionToCenterChildX();

		invalidate();
	}

	void trackMotionScrollY(int deltaY) {

		if (getChildCount() == 0) {
			return;
		}

		boolean toTop = deltaY < 0;

		int limitedDeltaY = getLimitedMotionScrollAmountY(toTop, deltaY);
		if (limitedDeltaY != deltaY) {
			// The above call returned a limited amount, so stop any
			// scrolls/flings
			mFlingRunnable.endFling(false);
			onFinishedMovement();
		}

		offsetChildrenTopAndBottom(limitedDeltaY);

		detachOffScreenChildrenY(toTop);

		if (toTop) {
			// If moved left, there will be empty space on the right
			fillToGalleryBottom();
		} else {
			// Similarly, empty space on the left
			fillToGalleryTop();
		}

		// Clear unused views
		mRecycler.clear();

		setSelectionToCenterChildY();

		invalidate();
	}

	int getLimitedMotionScrollAmountX(boolean motionToLeft, int deltaX) {
		int extremeItemPosition = motionToLeft ? mItemCount - 1 : 0;
		View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);

		if (extremeChild == null) {
			return deltaX;
		}

		int extremeChildCenter = getCenterOfViewX(extremeChild);
		int galleryCenter = getCenterOfGalleryX();

		if (motionToLeft) {
			if (extremeChildCenter <= galleryCenter) {
				// The extreme child is past his boundary point!
				return 0;
			}
		} else {
			if (extremeChildCenter >= galleryCenter) {
				// The extreme child is past his boundary point!
				return 0;
			}
		}
		int centerDifference = galleryCenter - extremeChildCenter;
		return motionToLeft ? Math.max(centerDifference, deltaX) : Math.min(
				centerDifference, deltaX);
	}

	int getLimitedMotionScrollAmountY(boolean motionToTop, int deltaY) {
		int extremeItemPosition = motionToTop ? mItemCount - 1 : 0;
		View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);

		if (extremeChild == null) {
			return deltaY;
		}

		int extremeChildCenter = getCenterOfViewY(extremeChild);
		int galleryCenter = getCenterOfGalleryX();
		if (motionToTop) {
			if (extremeChildCenter <= galleryCenter) {

				// The extreme child is past his boundary point!
				return 0;
			}
		} else {
			if (extremeChildCenter >= galleryCenter) {

				// The extreme child is past his boundary point!
				return 0;
			}
		}

		int centerDifference = galleryCenter - extremeChildCenter;

		return motionToTop ? Math.max(centerDifference, deltaY) : Math.min(
				centerDifference, deltaY);
	}

	/**
	 * Offset the horizontal location of all children of this view by the
	 * specified number of pixels.
	 * 
	 * @param offset
	 *            the number of pixels to offset
	 */
	private void offsetChildrenLeftAndRight(int offset) {
		for (int i = getChildCount() - 1; i >= 0; i--) {
			getChildAt(i).offsetLeftAndRight(offset);
		}
	}
	public void offsetChildrenTopAndBottom(int offset) {
		for (int i = getChildCount() - 1; i >= 0; i--) {
			getChildAt(i).offsetTopAndBottom(offset);
		}
	}

	/**
	 * @return The center of the given view.
	 */
	/**
	 * @return The center of this Gallery.
	 */
	private int getCenterOfGalleryX() {
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2
				+ getPaddingLeft();
	}

	private int getCenterOfGalleryY() {
		return (getHeight() - getPaddingTop() - getPaddingBottom()) / 2
				+ getPaddingTop();
	}

	/**
	 * @return The center of the given view.
	 */
	private static int getCenterOfViewX(View view) {
		return view.getLeft() + view.getWidth() / 2;
	}

	private static int getCenterOfViewY(View view) {
		return view.getTop() + view.getHeight() / 2;
	}

	/**
	 * Detaches children that are off the screen (i.e.: Gallery bounds).
	 * 
	 * @param toLeft
	 *            Whether to detach children to the left of the Gallery, or to
	 *            the right.
	 */
	private void detachOffScreenChildrenX(boolean toLeft) {
		int numChildren = getChildCount();
		int firstPosition = mFirstPosition;
		int start = 0;
		int count = 0;

		if (toLeft) {
			final int galleryLeft = getPaddingLeft();
			for (int i = 0; i < numChildren; i++) {
				final View child = getChildAt(i);
				if (child.getRight() >= galleryLeft) {
					break;
				} else {
					count++;
					mRecycler.put(firstPosition + i, child);
				}
			}
		} else {
			final int galleryRight = getWidth() - getPaddingRight();
			for (int i = numChildren - 1; i >= 0; i--) {
				final View child = getChildAt(i);
				if (child.getLeft() <= galleryRight) {
					break;
				} else {
					start = i;
					count++;
					mRecycler.put(firstPosition + i, child);
				}
			}
		}

		detachViewsFromParent(start, count);

		if (toLeft) {
			mFirstPosition += count;
		}
	}

	private void detachOffScreenChildrenY(boolean toTop) {
		int numChildren = getChildCount();
		int firstPosition = mFirstPosition;
		int start = 0;
		int count = 0;

		if (toTop) {
			final int galleryTop = getPaddingTop();
			for (int i = 0; i < numChildren; i++) {
				final View child = getChildAt(i);
				if (child.getBottom() >= galleryTop) {
					break;
				} else {
					count++;
					mRecycler.put(firstPosition + i, child);
				}
			}
		} else {
			final int galleryBottom = getHeight() - getPaddingBottom();
			for (int i = numChildren - 1; i >= 0; i--) {
				final View child = getChildAt(i);
				if (child.getTop() <= galleryBottom) {
					break;
				} else {
					start = i;
					count++;
					mRecycler.put(firstPosition + i, child);
				}
			}
		}

		detachViewsFromParent(start, count);

		if (toTop) {
			mFirstPosition += count;
		}
	}

	/**
	 * Scrolls the items so that the selected item is in its 'slot' (its center
	 * is the gallery's center).
	 */
	private void scrollIntoSlotsX() {

		if (getChildCount() == 0 || mSelectedChild == null)
			return;

		int selectedCenter = getCenterOfViewX(mSelectedChild);
		int targetCenter = getCenterOfGalleryX();

		int scrollAmount = targetCenter - selectedCenter;
		if (scrollAmount != 0) {
			mFlingRunnable.startUsingDistance(scrollAmount);
		} else {
			onFinishedMovement();
		}
	}

	private void scrollIntoSlotsY() {

		if (getChildCount() == 0 || mSelectedChild == null)
			return;

		int selectedCenter = getCenterOfViewY(mSelectedChild);
		int targetCenter = getCenterOfGalleryY();

		int scrollAmount = targetCenter - selectedCenter;
		if (scrollAmount != 0) {
			mFlingRunnable.startUsingDistance(scrollAmount);
		} else {
			onFinishedMovement();
		}
	}

	private void onFinishedMovement() {
		if (mSuppressSelectionChanged) {
			mSuppressSelectionChanged = false;

			// We haven't been callbacking during the fling, so do it now
			super.selectionChanged();
		}
		invalidate();
	}

	@Override
	void selectionChanged() {
		if (!mSuppressSelectionChanged) {
			super.selectionChanged();
		}
	}

	/**
	 * Looks for the child that is closest to the center and sets it as the
	 * selected child.
	 */
	private void setSelectionToCenterChildX() {

		View selView = mSelectedChild;
		if (mSelectedChild == null)
			return;

		int galleryCenter = getCenterOfGalleryX();

		// Common case where the current selected position is correct
		if (selView.getLeft() <= galleryCenter
				&& selView.getRight() >= galleryCenter) {
			return;
		}

		// TODO better search
		int closestEdgeDistance = Integer.MAX_VALUE;
		int newSelectedChildIndex = 0;
		for (int i = getChildCount() - 1; i >= 0; i--) {

			View child = getChildAt(i);

			if (child.getLeft() <= galleryCenter
					&& child.getRight() >= galleryCenter) {
				// This child is in the center
				newSelectedChildIndex = i;
				break;
			}

			int childClosestEdgeDistance = Math.min(
					Math.abs(child.getLeft() - galleryCenter),
					Math.abs(child.getRight() - galleryCenter));
			if (childClosestEdgeDistance < closestEdgeDistance) {
				closestEdgeDistance = childClosestEdgeDistance;
				newSelectedChildIndex = i;
			}
		}

		int newPos = mFirstPosition + newSelectedChildIndex;

		if (newPos != mSelectedPosition) {
			setSelectedPositionInt(newPos);
			setNextSelectedPositionInt(newPos);
			checkSelectionChanged();
		}
	}

	private void setSelectionToCenterChildY() {

		View selView = mSelectedChild;
		if (mSelectedChild == null)
			return;

		int galleryCenter = getCenterOfGalleryY();

		// Common case where the current selected position is correct
		if (selView.getTop() <= galleryCenter
				&& selView.getBottom() >= galleryCenter) {
			return;
		}

		// TODO better search
		int closestEdgeDistance = Integer.MAX_VALUE;
		int newSelectedChildIndex = 0;
		for (int i = getChildCount() - 1; i >= 0; i--) {

			View child = getChildAt(i);

			if (child.getTop() <= galleryCenter
					&& child.getBottom() >= galleryCenter) {
				// This child is in the center
				newSelectedChildIndex = i;
				break;
			}

			int childClosestEdgeDistance = Math.min(
					Math.abs(child.getTop() - galleryCenter),
					Math.abs(child.getBottom() - galleryCenter));
			if (childClosestEdgeDistance < closestEdgeDistance) {
				closestEdgeDistance = childClosestEdgeDistance;
				newSelectedChildIndex = i;
			}
		}

		int newPos = mFirstPosition + newSelectedChildIndex;

		if (newPos != mSelectedPosition) {
			setSelectedPositionInt(newPos);
			setNextSelectedPositionInt(newPos);
			checkSelectionChanged();
		}
	}
	/**
	 * Creates and positions all views for this Gallery.
	 * <p>
	 * We layout rarely, most of the time##takes care of repositioning, adding,
	 * and removing children.
	 * 
	 * @param delta
	 *            Change in the selected position. +1 means the selection is
	 *            moving to the right, so views are scrolling to the left. -1
	 *            means the selection is moving to the left.
	 */
	@Override
	void layout(int delta, boolean animate) {
		if (mIsScrollHoriz)
			layoutX(delta, animate);
		else
			layoutY(delta, animate);
	}
	private void getAutoColumnCount() {
		if (mChildViewHeight > 0) {
			if (mIsScrollHoriz) {
				int childrenHeight = getBottom() - getTop()
						- mSpinnerPadding.top - mSpinnerPadding.bottom;
				mColumnCount = childrenHeight
						/ (mChildViewHeight + mColumnSpace);
				mColumnSpaceIndeed = (childrenHeight - mChildViewHeight
						* mColumnCount)
						/ (mColumnCount + 1);
				// if(mColumnCount == 1)mColumnSpaceIndeed /=2;
				// else mColumnSpaceIndeed /= (mColumnCount-1);
			} else {
				int childrenWidth = getRight() - getLeft()
						- mSpinnerPadding.left - mSpinnerPadding.right;
				mColumnCount = childrenWidth / (mChildViewWidth + mColumnSpace);
				mColumnSpaceIndeed = (childrenWidth - mChildViewWidth
						* mColumnCount)
						/ (mColumnCount + 1);
				// if(mColumnCount == 1)mColumnSpaceIndeed /=2;
				// else mColumnSpaceIndeed /= (mColumnCount-1);
			}
		}
		if (mColumnCount < 1)
			mColumnCount = 1;
	}
	void layoutX(int delta, boolean animate) {
		int childrenLeft = mSpinnerPadding.left;
		int childrenWidth = getRight() - getLeft() - mSpinnerPadding.left
				- mSpinnerPadding.right;

		getAutoColumnCount();

		if (mDataChanged) {
			handleDataChanged();
		}

		// Handle an empty gallery by removing all views.
		if (mItemCount == 0) {
			resetList();
			return;
		}

		// Update to the new selected position.
		if (mNextSelectedPosition >= 0) {
			setSelectedPositionInt(mNextSelectedPosition);
		}

		// All views go in recycler while we are in layout
		recycleAllViews();

		// Clear out old views
		// removeAllViewsInLayout();
		detachAllViewsFromParent();

		/*
		 * These will be used to give initial positions to views entering the
		 * gallery as we scroll
		 */
		mRightMost = 0;
		mLeftMost = 0;

		// Make selected view and center it

		/*
		 * mFirstPosition will be decreased as we add views to the left later
		 * on. The 0 for x will be offset in a couple lines down.
		 */
		mFirstPosition = mSelectedPosition;
		View sel = makeAndAddViewX(mSelectedPosition, childrenLeft, 0, true);

		// Put the selected child in the center
		int selectedOffset = childrenLeft + (childrenWidth / 2)
				- (sel.getWidth() / 2);

		int iLeaveLeft = ((mSelectedPosition + mColumnCount) / mColumnCount - 1)
				* sel.getWidth();
		if (iLeaveLeft < childrenWidth / 2 - sel.getWidth() / 2) {
			selectedOffset = iLeaveLeft;
		} else {
			iLeaveLeft = (mItemCount - mSelectedPosition + mColumnCount)
					/ mColumnCount * sel.getWidth();
			if (iLeaveLeft < (sel.getWidth() / 2 + childrenWidth / 2)) {
				selectedOffset = iLeaveLeft;
			}
		}
		sel.offsetLeftAndRight(selectedOffset);
		fillToGalleryLeft();
		fillToGalleryRight();

		// Flush any cached views that did not get reused above
		mRecycler.clear();

		invalidate();
		checkSelectionChanged();

		mDataChanged = false;
		mNeedSync = false;
		setNextSelectedPositionInt(mSelectedPosition);

		updateSelectedItemMetadata();
	}

	void layoutY(int delta, boolean animate) {

		int childrenTop = mSpinnerPadding.top;
		int childrenBottom = getBottom() - getTop() - mSpinnerPadding.top
				- mSpinnerPadding.bottom;
		getAutoColumnCount();
		if (mDataChanged) {
			handleDataChanged();
		}

		// Handle an empty gallery by removing all views.
		if (mItemCount == 0) {
			resetList();
			return;
		}

		// Update to the new selected position.
		if (mNextSelectedPosition >= 0) {
			setSelectedPositionInt(mNextSelectedPosition);
		}

		// All views go in recycler while we are in layout
		recycleAllViews();

		// Clear out old views
		// removeAllViewsInLayout();
		detachAllViewsFromParent();

		/*
		 * These will be used to give initial positions to views entering the
		 * gallery as we scroll
		 */
		mBottomMost = 0;
		mTopMost = 0;

		// Make selected view and center it

		/*
		 * mFirstPosition will be decreased as we add views to the left later
		 * on. The 0 for x will be offset in a couple lines down.
		 */
		mFirstPosition = mSelectedPosition;
		View sel = makeAndAddViewY(mSelectedPosition, 0, 0, true);

		// Put the selected child in the center
		int selectedOffset = childrenTop + (childrenBottom / 2)
				- (sel.getHeight() / 2);

		int iLeaveLeft = ((mSelectedPosition + mColumnCount) / mColumnCount - 1)
				* sel.getHeight();
		if (iLeaveLeft < childrenBottom / 2 - sel.getHeight() / 2) {
			selectedOffset = iLeaveLeft;
		} else {
			iLeaveLeft = (mItemCount - mSelectedPosition + mColumnCount)
					/ mColumnCount * sel.getHeight();
			if (iLeaveLeft < (sel.getWidth() / 2 + childrenBottom / 2)) {
				selectedOffset = iLeaveLeft;
			}
		}

		sel.offsetTopAndBottom(selectedOffset);

		fillToGalleryTop();
		fillToGalleryBottom();

		// Flush any cached views that did not get reused above
		mRecycler.clear();

		invalidate();
		checkSelectionChanged();

		mDataChanged = false;
		mNeedSync = false;
		setNextSelectedPositionInt(mSelectedPosition);

		updateSelectedItemMetadata();
	}

	private void fillToGalleryLeft() {
		int itemSpacing = mSpacing;
		int galleryLeft = getPaddingLeft();

		// Set state for initial iteration
		View prevIterationView = getChildAt(0);
		int curPosition;
		int curRightEdge = 0;

		if (prevIterationView != null) {
			curPosition = mFirstPosition - 1;
			if ((mFirstPosition % mColumnCount) == 0) {
				curRightEdge = prevIterationView.getLeft() - itemSpacing;
			} else {
				curRightEdge = prevIterationView.getRight();
			}
		} else {
			// No children available!
			curPosition = 0;
			curRightEdge = getRight() - getLeft() - getPaddingRight();
			mShouldStopFling = true;
		}

		while (curRightEdge > galleryLeft && curPosition >= 0) {
			prevIterationView = makeAndAddViewX(curPosition, curPosition
					- mSelectedPosition, curRightEdge, false);

			// Remember some state
			mFirstPosition = curPosition;

			// Set state for next iteration
			if (curPosition % mColumnCount == 0) {
				curRightEdge = prevIterationView.getLeft() - itemSpacing;
			}
			curPosition--;
		}
	}

	private void fillToGalleryTop() {
		int itemSpacing = mSpacing;
		int galleryTop = getPaddingTop();

		// Set state for initial iteration
		View prevIterationView = getChildAt(0);
		int curPosition;
		int curBottomEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition - 1;
			if (mFirstPosition % mColumnCount == 0) {
				curBottomEdge = prevIterationView.getTop() - itemSpacing;
			} else {
				curBottomEdge = prevIterationView.getBottom();
			}
		} else {
			// No children available!
			curPosition = 0;
			curBottomEdge = getBottom() - getTop() - getPaddingBottom();
			mShouldStopFling = true;
		}

		while (curBottomEdge > galleryTop && curPosition >= 0) {
			// Set state for next iteration
			prevIterationView = makeAndAddViewY(curPosition, curPosition
					- mSelectedPosition, curBottomEdge, false);

			if (curPosition % mColumnCount == 0) {
				curBottomEdge = prevIterationView.getTop() - itemSpacing;
			}
			// Remember some state
			mFirstPosition = curPosition;
			curPosition--;
		}
	}

	private int fillToGalleryRight() {
		int itemSpacing = mSpacing;
		int galleryRight = getRight() - getLeft() - getPaddingRight();
		int numChildren = getChildCount();
		int numItems = mItemCount;
		int iLeavedRight = galleryRight;

		// Set state for initial iteration
		View prevIterationView = getChildAt(numChildren - 1);
		int curPosition;
		int curLeftEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition + numChildren;
			if (curPosition % mColumnCount == 0) {
				curLeftEdge = prevIterationView.getRight() + itemSpacing;
			} else {
				curLeftEdge = prevIterationView.getLeft();
			}
		} else {
			mFirstPosition = curPosition = mItemCount - 1;
			curLeftEdge = getPaddingLeft();
			mShouldStopFling = true;
		}

		while (curLeftEdge < galleryRight && curPosition < numItems) {
			prevIterationView = makeAndAddViewX(curPosition, curPosition
					- mSelectedPosition, curLeftEdge, true);
			// Set state for next iteration
			curPosition++;
			if (curPosition % mColumnCount == 0) {
				curLeftEdge = prevIterationView.getRight() + itemSpacing;
			}
		}
		return iLeavedRight;
	}

	private void fillToGalleryBottom() {
		int itemSpacing = mSpacing;
		int galleryBottom = getBottom() - getTop() - getPaddingBottom();
		int numChildren = getChildCount();
		int numItems = mItemCount;

		// Set state for initial iteration
		View prevIterationView = getChildAt(numChildren - 1);
		int curPosition;
		int curTopEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition + numChildren;
			if (curPosition % mColumnCount == 0) {
				curTopEdge = prevIterationView.getBottom() + itemSpacing;
			} else {
				curTopEdge = prevIterationView.getTop();
			}
		} else {
			mFirstPosition = curPosition = mItemCount - 1;
			curTopEdge = getPaddingTop();
			mShouldStopFling = true;
		}

		while (curTopEdge < galleryBottom && curPosition < numItems) {
			prevIterationView = makeAndAddViewY(curPosition, curPosition
					- mSelectedPosition, curTopEdge, true);

			curPosition++;
			// Set state for next iteration
			if (curPosition % mColumnCount == 0) {
				curTopEdge = prevIterationView.getBottom() + itemSpacing;
			}
		}
	}

	/**
	 * Obtain a view, either by pulling an existing view from the recycler or by
	 * getting a new one from the adapter. If we are animating, make sure there
	 * is enough information in the view's layout parameters to animate from the
	 * old to new positions.
	 * 
	 * @param position
	 *            Position in the gallery for the view to obtain
	 * @param offset
	 *            Offset from the selected position
	 * @param x
	 *            X-coordintate indicating where this view should be placed.
	 *            This will either be the left or right edge of the view,
	 *            depending on the fromLeft paramter
	 * @param fromLeft
	 *            Are we posiitoning views based on the left edge? (i.e.,
	 *            building from left to right)?
	 * @return A view that has been added to the gallery
	 */

	private View makeAndAddViewX(int position, int offset, int x,
			boolean fromLeft) {

		View child;

		if (!mDataChanged) {
			child = mRecycler.get(position);
			if (child != null) {
				// Can reuse an existing view
				int childLeft = child.getLeft();

				// Remember left and right edges of where views have been placed
				mRightMost = Math.max(mRightMost,
						childLeft + child.getMeasuredWidth());
				mLeftMost = Math.min(mLeftMost, childLeft);

				mChildViewHeight = child.getMeasuredHeight();
				mChildViewWidth = child.getMeasuredWidth();
				getAutoColumnCount();
				// Position the view
				setUpChildX(position, child, offset, x, fromLeft);

				return child;
			}
		}

		// Nothing found in the recycler -- ask the adapter for a view
		child = mAdapter.getView(position, null, this);

		mChildViewHeight = child.getMeasuredHeight();
		mChildViewWidth = child.getMeasuredWidth();
		getAutoColumnCount();
		// Position the view
		setUpChildX(position, child, offset, x, fromLeft);

		return child;
	}

	private View makeAndAddViewY(int position, int offset, int y,
			boolean fromTop) {

		View child;

		if (!mDataChanged) {
			child = mRecycler.get(position);
			if (child != null) {
				// Can reuse an existing view
				int childTop = child.getTop();

				// Remember left and right edges of where views have been placed
				mBottomMost = Math.max(mBottomMost,
						childTop + child.getMeasuredWidth());
				mTopMost = Math.min(mTopMost, childTop);

				mChildViewHeight = child.getMeasuredHeight();
				mChildViewWidth = child.getMeasuredWidth();
				getAutoColumnCount();
				// Position the view
				setUpChildY(position, child, offset, y, fromTop);

				return child;
			}
		}

		// Nothing found in the recycler -- ask the adapter for a view
		child = mAdapter.getView(position, null, this);

		mChildViewHeight = child.getMeasuredHeight();
		mChildViewWidth = child.getMeasuredWidth();
		getAutoColumnCount();
		// Position the view
		setUpChildY(position, child, offset, y, fromTop);

		return child;
	}

	/**
	 * Helper for makeAndAddView to set the position of a view and fill out its
	 * layout paramters.
	 * 
	 * @param child
	 *            The view to position
	 * @param offset
	 *            Offset from the selected position
	 * @param x
	 *            X-coordintate indicating where this view should be placed.
	 *            This will either be the left or right edge of the view,
	 *            depending on the fromLeft paramter
	 * @param fromLeft
	 *            Are we posiitoning views based on the left edge? (i.e.,
	 *            building from left to right)?
	 */
	private void setUpChildX(int position, View child, int offset, int x,
			boolean fromLeft) {

		// Respect layout params that are already in the view. Otherwise
		// make some up...
		GalleryHW.LayoutParams lp = (GalleryHW.LayoutParams) child
				.getLayoutParams();
		if (lp == null) {
			lp = (GalleryHW.LayoutParams) generateDefaultLayoutParams();
		}

		addViewInLayout(child, fromLeft ? -1 : 0, lp);

		child.setSelected(offset == 0);

		// Get measure specs
		int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
				mSpinnerPadding.top + mSpinnerPadding.bottom, lp.height);
		int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
				mSpinnerPadding.left + mSpinnerPadding.right, lp.width);

		// Measure child
		child.measure(childWidthSpec, childHeightSpec);

		int childLeft;
		int childRight;

		// Position vertically based on gravity setting
		int childTop = mColumnSpaceIndeed;// calculateTop(child, true);
		childTop += (position % mColumnCount)
				* (child.getMeasuredHeight() + mColumnSpaceIndeed);
		int childBottom = childTop + child.getMeasuredHeight();

		int width = child.getMeasuredWidth();
		if (fromLeft) {
			childLeft = x;
			childRight = childLeft + width;
		} else {
			childLeft = x - width;
			childRight = x;
		}

		child.layout(childLeft, childTop, childRight, childBottom);
	}

	private void setUpChildY(int position, View child, int offset, int y,
			boolean fromTop) {

		// Respect layout params that are already in the view. Otherwise
		// make some up...
		GalleryHW.LayoutParams lp = (GalleryHW.LayoutParams) child
				.getLayoutParams();
		if (lp == null) {
			lp = (GalleryHW.LayoutParams) generateDefaultLayoutParams();
		}

		addViewInLayout(child, fromTop ? -1 : 0, lp);

		child.setSelected(offset == 0);

		// Get measure specs
		int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
				mSpinnerPadding.top + mSpinnerPadding.bottom, lp.height);
		int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
				mSpinnerPadding.left + mSpinnerPadding.right, lp.width);

		// Measure child
		child.measure(childWidthSpec, childHeightSpec);

		int childTop;
		int childBottom;

		// Position vertically based on gravity setting
		int childLeft = mColumnSpaceIndeed;// calculateLeft(child, true);
		childLeft += (position % mColumnCount)
				* (child.getMeasuredWidth() + mColumnSpaceIndeed);
		int childRight = childLeft + child.getMeasuredWidth();

		int height = child.getMeasuredHeight();
		if (fromTop) {
			childTop = y;
			childBottom = childTop + height;
		} else {
			childTop = y - height;
			childBottom = y;
		}

		child.layout(childLeft, childTop, childRight, childBottom);
	}

	/**
	 * Figure out vertical placement based on mGravity
	 * 
	 * @param child
	 *            Child to place
	 * @return Where the top of the child should be
	 */
	private int calculateTop(View child, boolean duringLayout) {
		int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
		int childHeight = duringLayout ? child.getMeasuredHeight() : child
				.getHeight();

		int childTop = 0;

		switch (mGravity) {
			case Gravity.TOP :
				// childTop = mSpinnerPadding.top;
				// break;
			case Gravity.BOTTOM :
				// childTop = myHeight - mSpinnerPadding.bottom - childHeight;
				// break;
			case Gravity.CENTER_VERTICAL :
			default :
				int availableSpace = myHeight - mSpinnerPadding.bottom
						- mSpinnerPadding.top
						- (childHeight + mColumnSpaceIndeed) * mColumnCount
						+ mColumnSpace;
				childTop = mSpinnerPadding.top + (availableSpace / 2);
				break;
		}
		return childTop;
	}

	private int calculateLeft(View child, boolean duringLayout) {
		int myWidth = duringLayout ? getMeasuredWidth() : getWidth();
		int childWidth = duringLayout ? child.getMeasuredWidth() : child
				.getWidth();

		int childLeft = 0;
		switch (mGravity) {
			case Gravity.LEFT :
				// childLeft = mSpinnerPadding.left;
				// break;
			case Gravity.RIGHT :
				// childLeft = myWidth - mSpinnerPadding.right - childWidth;
				// break;
			case Gravity.CENTER_HORIZONTAL :
			default :
				int availableSpace = myWidth - mSpinnerPadding.right
						- mSpinnerPadding.left
						- (childWidth + mColumnSpaceIndeed) * mColumnCount
						+ mColumnSpace;
				childLeft = mSpinnerPadding.top + (availableSpace / 2);
				break;
		}
		return childLeft;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// Give everything to the gesture detector
		boolean retValue = mGestureDetector.onTouchEvent(event);

		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP) {
			// Helper method for lifted finger
			onUp();
		} else if (action == MotionEvent.ACTION_CANCEL) {
			onCancel();
		}

		return retValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onSingleTapUp(MotionEvent e) {

		if (mDownTouchPosition >= 0) {

			// An item tap should make it selected, so scroll to this child.
			if (mIsScrollHoriz) {
				scrollToChildX(mDownTouchPosition - mFirstPosition);
			} else {
				scrollToChildY(mDownTouchPosition - mFirstPosition);
			}

			// Also pass the click so the client knows, if it wants to.
			if (mShouldCallbackOnUnselectedItemClick
					|| mDownTouchPosition == mSelectedPosition) {
				performItemClick(mDownTouchView, mDownTouchPosition,
						mAdapter.getItemId(mDownTouchPosition));
			}

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		if (!mShouldCallbackDuringFling) {
			// We want to suppress selection changes

			// Remove any future code to set mSuppressSelectionChanged = false
			removeCallbacks(mDisableSuppressSelectionChangedRunnable);

			// This will get reset once we scroll into slots
			if (!mSuppressSelectionChanged)
				mSuppressSelectionChanged = true;
		}

		// Fling the gallery!
		if (mIsScrollHoriz) {
			mFlingRunnable.startUsingVelocity((int) -velocityX);
		} else {
			mFlingRunnable.startUsingVelocity((int) -velocityY);
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (mIsScrollHoriz) {
			if (localLOGV)
				Log.v(TAG, String.valueOf(e2.getX() - e1.getX()));
		} else {
			if (localLOGV)
				Log.v(TAG, String.valueOf(e2.getX() - e1.getX()));
		}
		/*
		 * Now's a good time to tell our parent to stop intercepting our events!
		 * The user has moved more than the slop amount, since GestureDetector
		 * ensures this before calling this method. Also, if a parent is more
		 * interested in this touch's events than we are, it would have
		 * intercepted them by now (for example, we can assume when a Gallery is
		 * in the ListView, a vertical scroll would not end up in this method
		 * since a ListView would have intercepted it by now).
		 */
		getParent().requestDisallowInterceptTouchEvent(true);

		// As the user scrolls, we want to callback selection changes so
		// related-
		// info on the screen is up-to-date with the gallery's selection
		if (!mShouldCallbackDuringFling) {
			if (mIsFirstScroll) {
				/*
				 * We're not notifying the client of selection changes during
				 * the fling, and this scroll could possibly be a fling. Don't
				 * do selection changes until we're sure it is not a fling.
				 */
				if (!mSuppressSelectionChanged)
					mSuppressSelectionChanged = true;
				postDelayed(mDisableSuppressSelectionChangedRunnable,
						SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT);
			}
		} else {
			if (mSuppressSelectionChanged)
				mSuppressSelectionChanged = false;
		}

		// Track the motion
		if (mIsScrollHoriz) {
			trackMotionScrollX(-1 * (int) distanceX);
		} else {
			trackMotionScrollY(-1 * (int) distanceY);
		}

		mIsFirstScroll = false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onDown(MotionEvent e) {

		// Kill any existing fling/scroll
		mFlingRunnable.stop(false);

		// Get the item's view that was touched
		mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());

		if (mDownTouchPosition >= 0) {
			mDownTouchView = getChildAt(mDownTouchPosition - mFirstPosition);
			mDownTouchView.setPressed(true);
		}

		// Reset the multiple-scroll tracking state
		mIsFirstScroll = true;

		// Must return true to get matching events for this down event.
		return true;
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_UP.
	 */
	void onUp() {

		if (mFlingRunnable.mScroller.isFinished()) {
			if (mIsScrollHoriz)
				scrollIntoSlotsX();
			else
				scrollIntoSlotsY();
		}

		dispatchUnpress();
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
	 */
	void onCancel() {
		onUp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLongPress(MotionEvent e) {

		if (mDownTouchPosition < 0) {
			return;
		}

		performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		long id = getItemIdAtPosition(mDownTouchPosition);
		dispatchLongPress(mDownTouchView, mDownTouchPosition, id);
	}

	// Unused methods from GestureDetector.OnGestureListener below

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onShowPress(MotionEvent e) {
	}

	// Unused methods from GestureDetector.OnGestureListener above

	private void dispatchPress(View child) {

		if (child != null) {
			child.setPressed(true);
		}

		setPressed(true);
	}

	private void dispatchUnpress() {

		for (int i = getChildCount() - 1; i >= 0; i--) {
			getChildAt(i).setPressed(false);
		}

		setPressed(false);
	}

	@Override
	public void dispatchSetSelected(boolean selected) {
		/*
		 * We don't want to pass the selected state given from its parent to its
		 * children since this widget itself has a selected state to give to its
		 * children.
		 */
	}

	@Override
	protected void dispatchSetPressed(boolean pressed) {

		// Show the pressed state on the selected child
		if (mSelectedChild != null) {
			mSelectedChild.setPressed(pressed);
		}
	}

	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		return mContextMenuInfo;
	}

	@Override
	public boolean showContextMenuForChild(View originalView) {

		final int longPressPosition = getPositionForView(originalView);
		if (longPressPosition < 0) {
			return false;
		}

		final long longPressId = mAdapter.getItemId(longPressPosition);
		return dispatchLongPress(originalView, longPressPosition, longPressId);
	}

	@Override
	public boolean showContextMenu() {

		if (isPressed() && mSelectedPosition >= 0) {
			int index = mSelectedPosition - mFirstPosition;
			View v = getChildAt(index);
			return dispatchLongPress(v, mSelectedPosition, mSelectedRowId);
		}

		return false;
	}

	private boolean dispatchLongPress(View view, int position, long id) {
		boolean handled = false;

		if (mOnItemLongClickListener != null) {
			handled = mOnItemLongClickListener.onItemLongClick(this,
					mDownTouchView, mDownTouchPosition, id);
		}

		if (!handled) {
			mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
			handled = super.showContextMenuForChild(this);
		}

		if (handled) {
			performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		}

		return handled;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// Gallery steals all key events
		return event.dispatch(this);
	}

	/**
	 * Handles left, right, and clicking
	 * 
	 * @see android.view.View#onKeyDown
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {

			case KeyEvent.KEYCODE_DPAD_LEFT :
				if (mIsScrollHoriz && movePreviousX()) {
					playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
				}
				return true;
			case KeyEvent.KEYCODE_DPAD_UP :
				if (!mIsScrollHoriz && movePreviousY()) {
					playSoundEffect(SoundEffectConstants.NAVIGATION_UP);
				}
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT :
				if (mIsScrollHoriz && moveNextX()) {
					playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
				}
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN :
				if (!mIsScrollHoriz && moveNextY()) {
					playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
				}
				return true;
			case KeyEvent.KEYCODE_DPAD_CENTER :
			case KeyEvent.KEYCODE_ENTER :
				mReceivedInvokeKeyDown = true;
				// fallthrough to default handling
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER :
			case KeyEvent.KEYCODE_ENTER : {

				if (mReceivedInvokeKeyDown) {
					if (mItemCount > 0) {

						dispatchPress(mSelectedChild);
						postDelayed(new Runnable() {
							@Override
							public void run() {
								dispatchUnpress();
							}
						}, ViewConfiguration.getPressedStateDuration());

						int selectedIndex = mSelectedPosition - mFirstPosition;
						performItemClick(getChildAt(selectedIndex),
								mSelectedPosition,
								mAdapter.getItemId(mSelectedPosition));
					}
				}

				// Clear the flag
				mReceivedInvokeKeyDown = false;

				return true;
			}
		}

		return super.onKeyUp(keyCode, event);
	}

	boolean movePreviousX() {
		if (mItemCount > 0 && mSelectedPosition > 0) {
			scrollToChildX(mSelectedPosition - mFirstPosition - 1);
			return true;
		} else {
			return false;
		}
	}

	boolean moveNextX() {
		if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
			scrollToChildX(mSelectedPosition - mFirstPosition + 1);
			return true;
		} else {
			return false;
		}
	}

	boolean movePreviousY() {
		if (mItemCount > 0 && mSelectedPosition > 0) {
			scrollToChildY(mSelectedPosition - mFirstPosition - 1);
			return true;
		} else {
			return false;
		}
	}

	boolean moveNextY() {
		if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
			scrollToChildY(mSelectedPosition - mFirstPosition + 1);
			return true;
		} else {
			return false;
		}
	}

	private boolean scrollToChildX(int childPosition) {
		View child = getChildAt(childPosition);
		if (child != null) {
			int distance = getCenterOfGalleryX() - getCenterOfViewX(child);
			mFlingRunnable.startUsingDistance(distance);
			return true;
		}
		return false;
	}

	private boolean scrollToChildY(int childPosition) {
		View child = getChildAt(childPosition);

		if (child != null) {
			int distance = getCenterOfGalleryY() - getCenterOfViewY(child);
			mFlingRunnable.startUsingDistance(distance);
			return true;
		}

		return false;
	}

	@Override
	void setSelectedPositionInt(int position) {
		super.setSelectedPositionInt(position);

		// Updates any metadata we keep about the selected item.
		updateSelectedItemMetadata();
	}

	private void updateSelectedItemMetadata() {

		View oldSelectedChild = mSelectedChild;

		View child = mSelectedChild = getChildAt(mSelectedPosition
				- mFirstPosition);
		if (child == null) {
			return;
		}

		child.setSelected(true);
		child.setFocusable(true);

		if (hasFocus()) {
			child.requestFocus();
		}

		// We unfocus the old child down here so the above hasFocus check
		// returns true
		if (oldSelectedChild != null) {

			// Make sure its drawable state doesn't contain 'selected'
			oldSelectedChild.setSelected(false);

			// Make sure it is not focusable anymore, since otherwise arrow keys
			// can make this one be focused
			oldSelectedChild.setFocusable(false);
		}

	}

	/**
	 * Describes how the child views are aligned.
	 * 
	 * @param gravity
	 * 
	 * @attr ref android.R.styleable#Gallery_gravity
	 */
	public void setGravity(int gravity) {
		if (mGravity != gravity) {
			mGravity = gravity;
			requestLayout();
		}
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		int selectedIndex = mSelectedPosition - mFirstPosition;

		// Just to be safe
		if (selectedIndex < 0)
			return i;

		if (i == childCount - 1) {
			// Draw the selected child last
			return selectedIndex;
		} else if (i >= selectedIndex) {
			// Move the children to the right of the selected child earlier one
			return i + 1;
		} else {
			// Keep the children to the left of the selected child the same
			return i;
		}
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

		/*
		 * The gallery shows focus by focusing the selected item. So, give focus
		 * to our selected item instead. We steal keys from our selected item
		 * elsewhere.
		 */
		if (gainFocus && mSelectedChild != null) {
			mSelectedChild.requestFocus(direction);
		}

	}

	/**
	 * Responsible for fling behavior. Use ## to initiate a fling. Each frame of
	 * the fling is handled in ##. A FlingRunnable will keep re-posting itself
	 * until the fling is done.
	 * 
	 */
	private class FlingRunnable implements Runnable {
		/**
		 * Tracks the decay of a fling scroll
		 */
		private Scroller mScroller;

		/**
		 * X value reported by mScroller on the previous fling
		 */
		private int mLastFlingXY;

		public FlingRunnable() {
			mScroller = new Scroller(getContext());
		}

		private void startCommon() {
			// Remove any pending flings
			removeCallbacks(this);
		}

		public void startUsingVelocity(int initialVelocity) {
			if (initialVelocity == 0)
				return;

			startCommon();
			if (mIsScrollHoriz) {
				int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
				mLastFlingXY = initialX;
				mScroller.fling(initialX, 0, initialVelocity, 0, 0,
						Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			} else {
				int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
				mLastFlingXY = initialY;
				mScroller.fling(0, initialY, 0, initialVelocity,
						Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 0);
			}
			post(this);
		}

		public void startUsingDistance(int distance) {
			if (distance == 0)
				return;

			startCommon();

			mLastFlingXY = 0;
			if (mIsScrollHoriz) {
				mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);
			} else {
				mScroller.startScroll(0, 0, 0, -distance, mAnimationDuration);
			}
			post(this);
		}

		public void stop(boolean scrollIntoSlots) {
			removeCallbacks(this);
			endFling(scrollIntoSlots);
		}

		private void endFling(boolean scrollIntoSlots) {
			/*
			 * Force the scroller's status to finished (without setting its
			 * position to the end)
			 */
			mScroller.forceFinished(true);

			if (scrollIntoSlots) {
				if (mIsScrollHoriz)
					scrollIntoSlotsX();
				else
					scrollIntoSlotsY();
			}
		}

		@Override
		public void run() {

			if (mItemCount == 0) {
				endFling(true);
				return;
			}

			mShouldStopFling = false;

			final Scroller scroller = mScroller;
			boolean more = scroller.computeScrollOffset();
			if (mIsScrollHoriz) {
				final int x = scroller.getCurrX();

				// Flip sign to convert finger direction to list items direction
				// (e.g. finger moving down means list is moving towards the
				// top)
				int delta = mLastFlingXY - x;

				// Pretend that each frame of a fling scroll is a touch scroll
				if (delta > 0) {
					// Moving towards the left. Use first view as
					// mDownTouchPosition
					mDownTouchPosition = mFirstPosition;

					// Don't fling more than 1 screen
					delta = Math.min(getWidth() - getPaddingLeft()
							- getPaddingRight() - 1, delta);
				} else {
					// Moving towards the right. Use last view as
					// mDownTouchPosition
					int offsetToLast = getChildCount() - 1;
					mDownTouchPosition = mFirstPosition + offsetToLast;

					// Don't fling more than 1 screen
					delta = Math.max(-(getWidth() - getPaddingRight()
							- getPaddingLeft() - 1), delta);
				}

				trackMotionScrollX(delta);

				if (more && !mShouldStopFling) {
					mLastFlingXY = x;
					post(this);
				} else {
					endFling(true);
				}
			} else {
				final int y = scroller.getCurrY();

				// Flip sign to convert finger direction to list items direction
				// (e.g. finger moving down means list is moving towards the
				// top)
				int delta = mLastFlingXY - y;

				// Pretend that each frame of a fling scroll is a touch scroll
				if (delta > 0) {
					// Moving towards the left. Use first view as
					// mDownTouchPosition
					mDownTouchPosition = mFirstPosition;

					// Don't fling more than 1 screen
					delta = Math.min(getHeight() - getPaddingTop()
							- getPaddingBottom() - 1, delta);
				} else {
					// Moving towards the right. Use last view as
					// mDownTouchPosition
					int offsetToLast = getChildCount() - 1;
					mDownTouchPosition = mFirstPosition + offsetToLast;

					// Don't fling more than 1 screen
					delta = Math.max(-(getHeight() - getPaddingBottom()
							- getPaddingTop() - 1), delta);
				}

				trackMotionScrollY(delta);

				if (more && !mShouldStopFling) {
					mLastFlingXY = y;
					post(this);
				} else {
					endFling(true);
				}
			}
		}
	}
	/**
	 * Gallery extends LayoutParams to provide a place to hold current
	 * Transformation information along with previous position/transformation
	 * info.
	 * 
	 */
	public static class LayoutParams extends ViewGroup.LayoutParams {
		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}

		public LayoutParams(int w, int h) {
			super(w, h);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}
}
