/**
 * =====================================================================
 *
 * @file  GalleryCoverflow.java
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
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

/**
 *
 */
@SuppressWarnings("unused")
public class GalleryCoverflow extends Gallery {

	// private static final String TAG = "Gallery3D";

	private static final int DEFAULT_FADING_EDGE_LENGTH = 90;
	private static final Camera mCamera = new Camera();

	private float[] mOffsetRotateCache, mOffsetScaleCache;
	private float mMaxUnselectedRotateAngle = 45.0f, mMaxSelectedScale = 1.0f,
			mMinUnselectedScale = 0.7f, mMinUnselectedScale1 = 0.4f;
	private int mStageOffsetMin = 0, mStageOffsetMax = 0;
	private int mItemWidth/* = 280 */;

	/**********************************
	 * CONSTRUCTORS
	 **********************************/

	/**
	 * Create a new Gallery3D instance.
	 * 
	 * @param context
	 */
	public GalleryCoverflow(Context context) {
		this(context, null);
	}

	/**
	 * Create a new Gallery3D instance.
	 * 
	 * @param context
	 * @param attrs
	 */
	public GalleryCoverflow(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Create a new Gallery3D instance.
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public GalleryCoverflow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setClipChildren(false);
		setClipToPadding(false);
		setDrawingCacheBackgroundColor(Color.BLACK);
		// setHorizontalFadingEdgeEnabled(true);
		setFadingEdgeLength(DEFAULT_FADING_EDGE_LENGTH);
		setStaticTransformationsEnabled(true);
	}

	/**********************************
	 * GETTERS/SETTERS
	 **********************************/

	/**
	 * The width of gallery items (must be uniform)
	 * 
	 * @param width
	 */
	public final void setItemWidth(int width) {
		mItemWidth = width;
		buildOffsetCache();
	}

	/**
	 * The amount that non-focused items will be rotated.
	 * 
	 * @param rotate
	 *            Valid rotation value between 0.0f and 90.0f.
	 */
	public final void setUnselectedRotate(float rotate) {
		mMaxUnselectedRotateAngle = rotate;
		buildOffsetCache();
	}

	/**
	 * The amount that non-selected items will be scaled.
	 * 
	 * @param scale
	 *            Scale value, may be greater than 1.0f.
	 */
	public final void setUnselectedScale(float scale) {
		mMinUnselectedScale = scale;
		buildOffsetCache();
	}

	/**
	 * Optional, over-scale the selected item (ie, 1.1f)
	 * 
	 * @param scale
	 */
	public final void setSelectedScale(float scale) {
		mMaxSelectedScale = scale;
		buildOffsetCache();
	}

	/**********************************
	 * PRIVATE METHODS
	 **********************************/

	/**
	 * Pre-calculate the range rotation angles and scaling factors for the
	 * non-static horizontal range of the Gallery.
	 */
	private final void buildOffsetCache() {
		final int galleryWidth = getWidth();
		if (galleryWidth < 10)
			return;
		mItemWidth = galleryWidth * 2 / 5;
		if (galleryWidth < 1 || mItemWidth < 1)
			return;

		final float unRotate = mMaxUnselectedRotateAngle, unScale = mMinUnselectedScale, maxScale = mMaxSelectedScale, unScaleRange = (mMaxSelectedScale - mMinUnselectedScale);

		final float centerline = galleryWidth / 2;
		final float itemHalfWidth = mItemWidth / 2;

		final int rangeMin = mStageOffsetMin = (int) (centerline - itemHalfWidth);
		final int rangeMax = mStageOffsetMax = (int) (centerline + itemHalfWidth);

		Log.i("name", mStageOffsetMin + "                   " + mStageOffsetMax);

		mOffsetRotateCache = new float[galleryWidth];
		mOffsetScaleCache = new float[galleryWidth];

		// child is to the left of the centerline, so translating only
		for (int i = 0; i < rangeMin; i++) {
			mOffsetRotateCache[i] = mMaxUnselectedRotateAngle;
			mOffsetScaleCache[i] = unScale;
		}

		// child is in range, so translating, rotating, and scaling
		float rangePct = 0.0f;
		for (int i = rangeMin; i < rangeMax; i++) {
			rangePct = ((centerline - i) / itemHalfWidth);
			mOffsetRotateCache[i] = (rangePct * unRotate);
			mOffsetScaleCache[i] = (rangePct > 0)
					? (maxScale - (rangePct * unScaleRange))
					: (maxScale + (rangePct * unScaleRange));
		}

		// child is to the right of the range, so translating only
		for (int i = rangeMax; i < galleryWidth; i++) {
			mOffsetRotateCache[i] = -unRotate;
			mOffsetScaleCache[i] = unScale;
		}

		// this is ugly but it works :)
		// smooth the rotation/scaling around the centerline by 3 pixelsin
		// either direction
		final int smoothMin = (int) (centerline - 3), smoothMax = (int) (centerline + 3);
		for (int i = smoothMin; i < smoothMax; i++) {
			mOffsetRotateCache[i] = 0.0f;
			mOffsetScaleCache[i] = maxScale;
		}

		// TODO: this was a quick proof-of-concept. we don't really need to
		// store
		// the entire width, only the range between rangeMin and rangeMax. The
		// getStaticTransformation method would then need to take this into
		// account
		// when it grabs the rotation/scale values (ie, subtract the range
		// lower-bound
		// from the child centerline, or something like that).
	}

	/**********************************
	 * OVERRIDE METHODS
	 **********************************/

	/**
	 * Per SDK: <b><i> Override this if your view is known to always be drawn on
	 * top of a solid color background, and needs to draw fading edges.
	 * Returning a non-zero color enables t he view system to optimize the
	 * drawing of the fading edges. If you do return a non-zero color, the alpha
	 * should be set to 0xFF. </i><b>
	 * 
	 * @return
	 */
	@Override
	public int getSolidColor() {
		return Color.BLACK;
	}

	/**
	 * Be sure the static rotation/scale cache is updated when the overall size
	 * changes
	 **/
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		buildOffsetCache();
	}

	/**
	 * Apply the necessary rotate and scale transformations to the provided
	 * child.
	 * 
	 * @param child
	 * @param t
	 * @return
	 */
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {

		// reset the transformation
		t.clear();
		t.setTransformationType(Transformation.TYPE_BOTH);

		// the offset of the child's centerline from the center
		// of the gallery
		final int centerX = child.getWidth() / 2, centerY = child.getHeight() / 2;
		Log.i("name", child.getClass().getSimpleName() + "    " + centerX
				+ "     " + centerY);
		// child.
		final int centerChild = (child.getLeft() + centerX);
		// Log.i("name", child.getLeft()+"                 "+child.getRight());
		final int leftChild = child.getLeft();
		final int rightChild = child.getRight();

		final Camera camera = mCamera;
		final Matrix matrix = t.getMatrix();

		// left of rotating, scaling range
		if (centerChild + child.getWidth() / 3 < mStageOffsetMin) {
			camera.save();
			camera.rotateY(mMaxUnselectedRotateAngle);

			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);

			matrix.preScale(mMinUnselectedScale1, mMinUnselectedScale1,
					centerX, centerY);

			// right of rotating, scaling range
		} else if (centerChild - child.getWidth() / 3 > mStageOffsetMax) {
			camera.save();
			camera.rotateY(-mMaxUnselectedRotateAngle);
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);

			matrix.preScale(mMinUnselectedScale1, mMinUnselectedScale1,
					centerX, centerY);

			// within the rotating, scaling range
		} else if (centerChild < mStageOffsetMin) {
			camera.save();
			camera.rotateY(mMaxUnselectedRotateAngle);
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);
			matrix.preScale(mMinUnselectedScale, mMinUnselectedScale, centerX,
					centerY);

			// right of rotating, scaling range
		} else if (centerChild > mStageOffsetMax) {
			camera.save();
			camera.rotateY(-mMaxUnselectedRotateAngle);
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);

			matrix.preScale(mMinUnselectedScale, mMinUnselectedScale, centerX,
					centerY);

			// within the rotating, scaling range
		} else {
			camera.save();
			camera.rotateY(mOffsetRotateCache[centerChild]);
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);

			matrix.preScale(mOffsetScaleCache[centerChild],
					mOffsetScaleCache[centerChild], centerX, centerY);

		}

		return true;
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		int selectedIndex = getSelectedItemPosition()
				- getFirstVisiblePosition();

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

}
