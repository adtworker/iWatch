package com.adtworker.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.sql.Time;
import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adtworker.mail.ImageManager.IMAGE_PATH_TYPE;
import com.adtworker.mail.constants.Constants;
import com.adtworker.mail.util.AdUtils;
import com.adview.AdViewInterface;
import com.android.camera.CropImage;

public class WatchActivity extends Activity implements AdViewInterface {

	private final Handler mHandler = new Handler();
	private int mImageViewCurrent = 0;
	private final ImageView[] mImageViews = new ImageView[2];
	private final Random mRandom = new Random(System.currentTimeMillis());
	private final ProgressBarReceiver mProgressbarRecv = new ProgressBarReceiver();
	private final ButtonStateReceiver mButtonStateRecv = new ButtonStateReceiver();
	private CoverFlow mCoverFlow;
	private TextView mBtnPrev;
	private TextView mBtnNext;
	private TextView mBtnDisp;
	private TextView mBtnClock;
	private LinearLayout mAdLayout;
	private ViewGroup mClockLayout;
	private View mClock = null;

	private int mAnimationIndex = -1;
	private Animation[] mSlideShowInAnimation;
	private Animation[] mSlideShowOutAnimation;

	private final String TAG = "WatchActivity";
	public final static int CLICKS_TO_HIDE_AD = 1;
	private final ScaleType DEFAULT_SCALETYPE = ScaleType.FIT_CENTER;
	private final ScaleType ALTER_SCALETYPE = ScaleType.CENTER_INSIDE;
	// private final ScaleType ALTER_SCALETYPE = ScaleType.CENTER_CROP;
	private ImageView.ScaleType mScaleType = DEFAULT_SCALETYPE;
	private final ImageManager mImageManager = WatchApp.getImageManager();

	private boolean bStarted = false;
	private boolean bSetAPos = false;
	private boolean bClickCoverFlow = false;
	private int mFace = -1;
	private int mStep = 1;
	private int iAdClick = 0;
	private boolean bKeyBackIn2Sec = false;
	private boolean bLargePicLoaded = false;
	private GestureDetector mGestureDetector;
	private GestureDetector mClockGestureDetector;
	private ProgressDialog mProcessDialog;
	public ProgressBar mProgressBar;
	public ProgressBar mProgressBar2;
	public ProgressBar mProgressIcon;
	private SharedPreferences mSharedPref;

	final static String PREFERENCES = "iWatch";
	final static String PREF_CLOCK_FACE = "face";
	final static String PREF_PIC_CODE = "pic_code";
	final static String PREF_LAST_CODE = "last_code";
	final static String PREF_FULL_SCR = "full_screen";
	final static String PREF_AUTOHIDE_CLOCK = "autohide_clock";
	final static String PREF_AUTOHIDE_AD = "autohide_ad";
	final static String PREF_AUTOHIDE_SB = "autohide_sb";
	final static String PREF_AD_CLICK_TIME = "ad_click_time";
	final static String PREF_BOSS_KEY = "boss_key";
	final static String PREF_PIC_FULL_FILL = "pic_fullfill";
	final static String PREF_WP_FULL_FILL = "wp_fullfill";
	final static String PREF_SLIDE_ANIM = "slide_anim";
	final static String PREF_NETIMG_RES = "netimg_res";
	final static String PREF_AUTO_ROTATE = "auto_rotate";

	private final static int[] CLOCKS = {R.layout.clock_no_dial,
			R.layout.clock_appwidget, R.layout.clock_basic_bw,
			R.layout.clock_basic_bw1, R.layout.clock_basic_bw3,
			R.layout.clock_googly, R.layout.clock_googly1,
			R.layout.clock_googly3, R.layout.clock_droid2,
			R.layout.clock_droid2_1, R.layout.clock_droid2_2,
			R.layout.clock_droid2_3, R.layout.clock_droids,
			R.layout.clock_droids1, R.layout.clock_droids2,
			R.layout.clock_droids3, R.layout.digital_clock};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Log.v(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mImageViews[0] = (ImageView) findViewById(R.id.picView1);
		mImageViews[1] = (ImageView) findViewById(R.id.picView2);
		mCoverFlow = (CoverFlow) findViewById(R.id.gallery);
		mCoverFlow.setVisibility(View.GONE);
		// mCoverFlow.setMaxRotationAngle(75);

		mBtnPrev = (TextView) findViewById(R.id.btnPrev);
		mBtnNext = (TextView) findViewById(R.id.btnNext);
		mBtnDisp = (TextView) findViewById(R.id.btnDisp);
		mBtnClock = (TextView) findViewById(R.id.btnClock);
		mBtnPrev.setVisibility(View.GONE);
		mBtnDisp.setEnabled(false);

		mSharedPref = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		mAdLayout = (LinearLayout) findViewById(R.id.adLayout);
		mClockLayout = (ViewGroup) findViewById(R.id.clockLayout);
		mProgressBar = (ProgressBar) findViewById(R.id.prgbar1);
		mProgressBar2 = (ProgressBar) findViewById(R.id.prgbar2);
		// mProgressBar.setVisibility(View.GONE);
		mProgressBar2.setVisibility(View.GONE);
		mProgressIcon = (ProgressBar) findViewById(R.id.prgIcon);
		mProgressIcon.setVisibility(View.GONE);

		mClockGestureDetector = new GestureDetector(this,
				new MyClockGestureListener());
		mClockLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mClockGestureDetector.onTouchEvent(event);
				return true;
			}
		});

		mGestureDetector = new GestureDetector(this, new MyGestureListener());
		OnTouchListener rootListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mGestureDetector.onTouchEvent(event);
				return true;
			}
		};

		for (ImageView iv : mImageViews) {
			iv.setOnTouchListener(rootListener);
		}

		mCoverFlow.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				bClickCoverFlow = true;
				int delta = 0;
				if (position > mImageManager.getCurrent())
					delta = 1;
				else if (position < mImageManager.getCurrent())
					delta = -1;
				else {
					bClickCoverFlow = false;
					// mCoverFlow.setVisibility(View.GONE);
					// mCoverFlow
					// .startAnimation(makeInAnimation(R.anim.transition_out));

					return;
				}
				mImageManager.setCurrent(position - delta);
				WatchActivity.this.goNextorPrev(delta);
				// mCoverFlow.setVisibility(View.GONE);
				// mCoverFlow
				// .startAnimation(makeInAnimation(R.anim.transition_out));

			}
		});

		WebView wv = (WebView) findViewById(R.id.webView);
		wv.getSettings().setSupportZoom(true);
		wv.getSettings().setBuiltInZoomControls(true);
		wv.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);

		setupButtons();

		mSlideShowInAnimation = new Animation[]{
				makeInAnimation(R.anim.transition_in),
				makeInAnimation(R.anim.slide_in),
				makeInAnimation(R.anim.slide_in_vertical),
				makeInAnimation(R.anim.slide_in_r),
				makeInAnimation(R.anim.slide_in_vertical_r),};

		mSlideShowOutAnimation = new Animation[]{
				makeOutAnimation(R.anim.transition_out),
				makeOutAnimation(R.anim.slide_out),
				makeOutAnimation(R.anim.slide_out_vertical),
				makeOutAnimation(R.anim.slide_out_r),
				makeOutAnimation(R.anim.slide_out_vertical_r),};

		mAnimationIndex = mSharedPref.getInt(PREF_SLIDE_ANIM, 0);

		AdUtils.setupAdLayout(this, mAdLayout, true);
		mHandler.postDelayed(mShowAndHideAds, 20000);
	}

	private final Runnable mShowAndHideAds = new Runnable() {
		@Override
		public void run() {
			boolean bVisible = getLayoutVisibility(R.id.adLayout);
			setLayoutVisibility(R.id.adLayout, !bVisible);
			mHandler.postDelayed(mShowAndHideAds, 20000);
		}
	};

	@SuppressWarnings("unused")
	private final Runnable mCheck2ShowAD = new Runnable() {
		@Override
		public void run() {
			check2showAD();
			mHandler.postDelayed(mCheck2ShowAD, 60000); // check every 60sec
		}
	};

	public void setImageView(Bitmap bm) {
		if (bm == null || mImageManager == null)
			return;

		mImageViews[mImageViewCurrent].setImageBitmap(bm);
		mImageViews[mImageViewCurrent].setScaleType(mScaleType);

		if (Constants.DEBUG) {
			TextView tv = (TextView) findViewById(R.id.picName);
			tv.setText(String.format("%d/%d, %dx%d",
					mImageManager.getCurrent() + 1,
					mImageManager.getImageListSize(), bm.getWidth(),
					bm.getHeight()));
		}
	}

	private final Runnable mUpdateImageView = new Runnable() {
		@Override
		public void run() {

			if (mSharedPref.getBoolean(PREF_BOSS_KEY, false)) {
				if (getClockVisibility()) {
					setClockVisibility(false);
				}
			}

			ImageView oldView = mImageViews[mImageViewCurrent];
			if (++mImageViewCurrent == mImageViews.length) {
				mImageViewCurrent = 0;
			}
			ImageView newView = mImageViews[mImageViewCurrent];
			newView.setVisibility(View.VISIBLE);

			Bitmap bm;
			if (!bStarted || bSetAPos) {
				bm = mImageManager.getCurrentBitmap();
				bStarted = true;
				bSetAPos = false;
			} else {
				bm = mImageManager.getImageBitmap(mStep);
			}
			if (!bClickCoverFlow) {
				mCoverFlow.setSelection(mImageManager.getCurrent());
			} else {
				bClickCoverFlow = false;
			}

			if (bm != null) {
				TextView tv;
				if (Constants.DEBUG) {
					tv = (TextView) findViewById(R.id.picName);
					tv.setText(String.format("%d/%d, %dx%d",
							mImageManager.getCurrent() + 1,
							mImageManager.getImageListSize(), bm.getWidth(),
							bm.getHeight()));
				}

				String urlRef = mImageManager.getCurrentStrRefUrl();
				tv = (TextView) findViewById(R.id.btnRefs);
				if (urlRef != null)
					tv.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// Intent intent = new Intent(Intent.ACTION_VIEW,
							// Uri
							// .parse(mImageManager.getCurrentStrRefUrl()));
							// startActivity(intent);

							WebView wv = (WebView) findViewById(R.id.webView);
							wv.loadUrl(mImageManager.getCurrentStrRefUrl());
							wv.setVisibility(View.VISIBLE);
							ScaleOutAnimation(wv);
						}
					});
				tv.setVisibility(urlRef != null ? View.VISIBLE : View.GONE);

				findViewById(R.id.btnSource).setOnClickListener(
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								// Intent intent = new Intent(
								// Intent.ACTION_VIEW,
								// Uri.parse(mImageManager.getCurrentStr()));
								// startActivity(intent);

								WebView wv = (WebView) findViewById(R.id.webView);
								wv.loadUrl(mImageManager.getCurrentStr());
								wv.setVisibility(View.VISIBLE);
								ScaleOutAnimation(wv);
							}
						});
				findViewById(R.id.btnSource).setVisibility(
						(urlRef != null) ? View.VISIBLE : View.GONE);
			}

			if (mSharedPref.getBoolean(PREF_PIC_FULL_FILL, true)) {
				mScaleType = DEFAULT_SCALETYPE;
			}
			newView.setScaleType(mScaleType);
			newView.setImageBitmap(bm);
			newView.scrollTo(0, 0);

			if (mAnimationIndex >= 0) {
				int animation = mAnimationIndex;
				if (mAnimationIndex > 0) {
					animation = mAnimationIndex + (1 - mStep);
				}
				Animation aIn = mSlideShowInAnimation[animation];
				newView.startAnimation(aIn);
				newView.setVisibility(View.VISIBLE);
				Animation aOut = mSlideShowOutAnimation[animation];
				oldView.setVisibility(View.INVISIBLE);
				oldView.startAnimation(aOut);
			} else {
				newView.setVisibility(View.VISIBLE);
				oldView.setVisibility(View.INVISIBLE);
			}

			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
			if (bm != null
					&& (bm.getWidth() > displayMetrics.widthPixels || bm
							.getHeight() > displayMetrics.heightPixels)) {
				bLargePicLoaded = true;
			} else {
				bLargePicLoaded = false;
			}
		}
	};

	private final Runnable mCheckingNetworkInit = new Runnable() {
		@Override
		public void run() {

			if (mImageManager == null) {
				mHandler.removeCallbacks(mCheckingNetworkInit);
				return;
			}

			if (!mImageManager.isInitInProcess()) {

				if (mImageManager.isInitListFailed()) {
					Toast.makeText(WatchActivity.this,
							getString(R.string.failed_network),
							Toast.LENGTH_SHORT).show();
					mProgressBar.setProgress(0);
				} else {

					mCoverFlow.setAdapter(new ImageAdapter(WatchActivity.this));
					if (bStarted) {
						initStartIndex();
						mImageManager
								.setCurrent(mImageManager.getCurrent() - 1);
						goNextorPrev(1);
					}
				}
			} else {
				mHandler.removeCallbacks(mCheckingNetworkInit);
				mHandler.postDelayed(mCheckingNetworkInit, 500);
			}
		}
	};

	@Override
	public void onStart() {
		Log.v(TAG, "onStart()");
		super.onStart();

		if (mSharedPref.getBoolean(PREF_AUTO_ROTATE, false)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		registerReceiver(mProgressbarRecv, new IntentFilter(
				Constants.SET_PROGRESSBAR));
		registerReceiver(mButtonStateRecv, new IntentFilter(
				Constants.SET_BUTTONSTATE));
	}

	@Override
	public void onResume() {
		Log.v(TAG, "onResume()");
		super.onResume();

		int face = mSharedPref.getInt(PREF_CLOCK_FACE, 0);
		if (mFace != face) {
			if (face < 0 || face >= CLOCKS.length)
				mFace = 0;
			else
				mFace = face;
			inflateClock();
		}

		check2showAD();

		if (mSharedPref.getBoolean(PREF_AUTOHIDE_SB, false)) {
			setSBVisibility(false);
		} else {
			setSBVisibility(getMLVisibility());
		}

		if (mSharedPref.getBoolean(PREF_BOSS_KEY, false)) {
			Boolean bClockVisible = getClockVisibility();
			if (bClockVisible) {
				mImageViews[mImageViewCurrent].setVisibility(bClockVisible
						? View.GONE
						: View.VISIBLE);
			}
		}

		mAnimationIndex = mSharedPref.getInt(PREF_SLIDE_ANIM, 0);

		if (bStarted
				&& mImageManager.isInitInProcess()
				&& mImageManager.getImagePathType() == IMAGE_PATH_TYPE.REMOTE_HTTP_URL)
			mHandler.postDelayed(mCheckingNetworkInit, 500);

		if (mProgressBar.getProgress() != 0)
			mProgressBar.setProgress(0);
	}

	@Override
	public void onPause() {
		Log.v(TAG, "onPause()");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.v(TAG, "onStop()");
		super.onStop();

		unregisterReceiver(mProgressbarRecv);
		unregisterReceiver(mButtonStateRecv);
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy()");
		super.onDestroy();

		if (mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS
				&& mImageManager.getCurrent() != ImageManager.INVALID_PIC_INDEX) {

			// save offset when browsing local assets
			Log.v(TAG, "Save current image index " + mImageManager.getCurrent()
					+ " to shared pref");
			mSharedPref.edit()
					.putInt(PREF_LAST_CODE, mImageManager.getCurrent())
					.commit();
		}

		for (int i = 0; i < mCoverFlow.getCount(); i++) {
			ImageView v = (ImageView) mCoverFlow.getChildAt(i);
			if (v != null) {
				if (v.getDrawable() != null)
					v.getDrawable().setCallback(null);
			}
		}

		WatchApp.getInstance().recycle();
	}

	@Override
	public void onClickAd() {
		Log.v(TAG, "onClickAd()");
		if (mSharedPref.getBoolean(PREF_AUTOHIDE_AD, false)) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					iAdClick = 0;
				}
			}, 5000);

			if (++iAdClick >= CLICKS_TO_HIDE_AD) {
				Log.d(TAG, "User just clicked AD.");
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						setAdVisibility(false);
						Time time = new Time(System.currentTimeMillis());
						Editor ed = mSharedPref.edit();
						ed.putString(PREF_AD_CLICK_TIME, time.toString())
								.commit();
					}
				}, 2000);
			}
		}

		// get phone info for test
		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();
		Log.v(TAG, "Test IMEI is " + imei);
		WifiManager wifi = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		String macAddr = info.getMacAddress();
		Log.v(TAG, "Test MAC is " + macAddr);
	}

	@Override
	public void onDisplayAd() {
		// Log.v(TAG, "onDisplayAd()");
		check2showAD();
	}

	private void check2showAD() {
		// autohide_ad is checked and within an hour, do hide AD
		if (mSharedPref.getBoolean(PREF_AUTOHIDE_AD, false)) {
			String timeStr = mSharedPref.getString(PREF_AD_CLICK_TIME, "");
			if (timeStr.length() != 0) {
				Time time = new Time(System.currentTimeMillis());
				Time time2Cmp = new Time(time.getHours() - 1,
						time.getMinutes(), time.getSeconds());
				Time timeClick = Time.valueOf(timeStr);

				if (timeClick.after(time2Cmp)) {
					// Log.v(TAG, "Hiding AD Layout.");
					setAdVisibility(false);
					return;
				} else {
					Log.v(TAG, "Removing click time tag.");
					Editor ed = mSharedPref.edit();
					ed.remove(PREF_AD_CLICK_TIME).commit();
				}
			}
		}
		// Log.v(TAG, "Showing AD Layout.");
		setAdVisibility(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case 1 :
				if (resultCode != ImageManager.INVALID_PIC_INDEX
						&& resultCode != mImageManager.getCurrent()) {
					mImageManager.setCurrent(resultCode);
					bSetAPos = true;
					goNextorPrev(1);
				}
				break;

			default :
				break;
		}
	}

	private void setupButtons() {

		mBtnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				goNextorPrev(-1);
			}
		});

		mBtnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				goNextorPrev(1);
			}
		});

		mBtnDisp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(WatchActivity.this, MyGallery.class);
				startActivityForResult(intent, 1);
			}
		});

		mBtnClock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean bClockVisible = getClockVisibility();
				setClockVisibility(!bClockVisible);

				if (mSharedPref.getBoolean(PREF_BOSS_KEY, false)) {
					mImageViews[mImageViewCurrent].setVisibility(bClockVisible
							? View.VISIBLE
							: View.GONE);
				}
			}
		});

		findViewById(R.id.btnNewSearch).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						DisplayMetrics display = getResources()
								.getDisplayMetrics();
						int width = display.widthPixels, height = display.heightPixels;
						String imgres = mSharedPref.getString(PREF_NETIMG_RES,
								"");
						if (!imgres.isEmpty()) {
							String[] results = imgres.split("x");
							if (results.length == 2) {
								width = Integer.parseInt(results[0]);
								height = Integer.parseInt(results[1]);
							}
						}
						mImageManager.setQueryImgSize(width, height);
						mImageManager.setQueryKeyword("美女");
						mImageManager.reinitImageList();
					}
				});

		findViewById(R.id.btnMoreSearch).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						mImageManager.appendImageList();
					}
				});
	}
	private void initStartIndex() {
		if (mImageManager.getCurrent() != ImageManager.INVALID_PIC_INDEX)
			return;
		int size = mImageManager.getImageListSize();
		if (0 == size)
			return;

		mImageManager.setCurrent(mRandom.nextInt(size));

		if (mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
			int index = mSharedPref.getInt(PREF_LAST_CODE,
					ImageManager.INVALID_PIC_INDEX);
			if (index != ImageManager.INVALID_PIC_INDEX)
				mImageManager.setCurrent(index);
		} else {
			mImageManager.setCurrent(0);
		}
		Log.d(TAG, "initStartIndex(): start from " + mImageManager.getCurrent());
	}

	private void goNextorPrev(int step) {
		if (mImageManager.getImageListSize() == 0)
			return;

		if ((step > 0 && !bStarted) || bSetAPos) {

			if (!bSetAPos) {
				initStartIndex();
			}

			if (mSharedPref.getBoolean(PREF_AUTOHIDE_CLOCK, true)) {
				setClockVisibility(false);
			}

			mCoverFlow.setAdapter(new ImageAdapter(this));

			mBtnNext.setText(getResources().getString(R.string.strNext));
			mBtnPrev.setVisibility(View.VISIBLE);
			mBtnDisp.setEnabled(true);
		}

		mStep = step;
		mHandler.post(mUpdateImageView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.menu_toggle_clock).setTitle(
				getClockVisibility()
						? R.string.hide_clock
						: R.string.show_clock);
		menu.findItem(R.id.menu_toggle_clock).setVisible(false);

		menu.findItem(R.id.menu_toggle_mode)
				.setTitle(
						mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS
								? R.string.remote_mode
								: R.string.local_mode);

		menu.findItem(R.id.menu_full_screen).setTitle(
				getMLVisibility()
						? R.string.full_screen
						: R.string.exit_full_screen);

		if (mImageManager.getCurrent() == ImageManager.INVALID_PIC_INDEX) {
			menu.findItem(R.id.menu_set_wallpaper).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_set_wallpaper).setEnabled(true);
		}

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_toggle_clock :
				setClockVisibility(!getClockVisibility());
				break;

			case R.id.menu_toggle_mode :

				if (mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS) {

					DisplayMetrics display = getResources().getDisplayMetrics();
					int width = display.widthPixels, height = display.heightPixels;
					String imgres = mSharedPref.getString(PREF_NETIMG_RES, "");
					if (!imgres.isEmpty()) {
						String[] results = imgres.split("x");
						if (results.length == 2) {
							width = Integer.parseInt(results[0]);
							height = Integer.parseInt(results[1]);
						}
					}
					mImageManager.setQueryKeyword("美女");
					mImageManager.setQueryImgSize(width, height);
					mImageManager.setSearchPages(2);
					mImageManager
							.setImagePathType(IMAGE_PATH_TYPE.REMOTE_HTTP_URL);
					mHandler.postDelayed(mCheckingNetworkInit, 0);

				} else {
					mImageManager
							.setImagePathType(IMAGE_PATH_TYPE.LOCAL_ASSETS);
					initStartIndex();
					mCoverFlow.setAdapter(new ImageAdapter(WatchActivity.this));
					if (bStarted) {
						mImageManager
								.setCurrent(mImageManager.getCurrent() - 1);
						goNextorPrev(1);
					}
				}

				boolean bRemoteAlbum = mImageManager.getImagePathType() == IMAGE_PATH_TYPE.REMOTE_HTTP_URL;
				findViewById(R.id.btnNewSearch).setVisibility(
						bRemoteAlbum ? View.VISIBLE : View.GONE);
				findViewById(R.id.btnMoreSearch).setVisibility(
						bRemoteAlbum ? View.VISIBLE : View.GONE);

				break;

			case R.id.menu_full_screen :
				setMLVisibility(!getMLVisibility());
				break;

			case R.id.menu_settings :
				startActivity(new Intent(this, Settings.class));
				break;

			case R.id.menu_set_livewallpaper :
				Editor myEdit = mSharedPref.edit();
				if (mImageManager.getCurrent() != ImageManager.INVALID_PIC_INDEX) {
					String pic_code;
					if (mImageManager.isCurrentAsset()) {
						pic_code = mImageManager.getCurrentStr();
					} else {
						pic_code = mImageManager.getCurrentStrLocal();
					}
					Log.d(TAG, "saving pic_code " + pic_code);
					myEdit.putString(PREF_PIC_CODE, pic_code);

				} else {
					// remove the preference to set default clock as live
					// wall paper
					myEdit.remove(PREF_PIC_CODE);
				}
				myEdit.commit();

				Toast.makeText(this, getString(R.string.help_livewallpaper),
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
				startActivity(intent);
				break;

			case R.id.menu_set_wallpaper :
				mProcessDialog = ProgressDialog.show(this,
						getString(R.string.set_wallpaper_title),
						getString(R.string.set_wallpaper_msg), true);

				new Thread() {
					@Override
					public void run() {
						try {
							setWallpaper();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							mProcessDialog.dismiss();
						}
					}
				}.start();

				break;

			default :

		}
		return super.onOptionsItemSelected(item);
	}
	private boolean getLayoutVisibility(int id) {
		LinearLayout layout = (LinearLayout) findViewById(id);
		return layout.getVisibility() == View.VISIBLE;
	}

	private void setLayoutVisibility(int id, boolean bVisibility) {
		LinearLayout layout = (LinearLayout) findViewById(id);
		layout.setVisibility(bVisibility ? View.VISIBLE : View.GONE);
	}

	private boolean getClockVisibility() {
		return getLayoutVisibility(R.id.clockLayout);
	}

	private void setClockVisibility(boolean bVisibility) {
		setLayoutVisibility(R.id.clockLayout, bVisibility);
		mBtnClock.setText(!bVisibility
				? R.string.show_clock
				: R.string.hide_clock);
	}

	private boolean getMLVisibility() {
		return getLayoutVisibility(R.id.mainLayout);
	}

	private void setMLVisibility(boolean bVisibility) {
		((LinearLayout) findViewById(R.id.mainLayout))
				.startAnimation(bVisibility ? AnimationUtils.loadAnimation(
						this, R.anim.footer_appear) : AnimationUtils
						.loadAnimation(this, R.anim.footer_disappear));

		((LinearLayout) findViewById(R.id.mainLayoutTop))
				.startAnimation(bVisibility ? AnimationUtils.loadAnimation(
						this, R.anim.header_appear) : AnimationUtils
						.loadAnimation(this, R.anim.header_disappear));

		setLayoutVisibility(R.id.mainLayout, bVisibility);
		setLayoutVisibility(R.id.mainLayoutTop, bVisibility);
		if (mSharedPref.getBoolean(PREF_AUTOHIDE_SB, false)) {
			bVisibility = false;
		}
		setSBVisibility(bVisibility);
	}

	private void setSBVisibility(boolean bVisibility) {
		if (bVisibility) {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	@SuppressWarnings("unused")
	private boolean getAdVisibility() {
		return getLayoutVisibility(R.id.adLayout);
	}

	private void setAdVisibility(boolean bVisibility) {
		setLayoutVisibility(R.id.adLayout, bVisibility);
	}

	private void EnableNextPrevButtons(boolean enabled) {
		mBtnPrev.setEnabled(enabled);
		mBtnNext.setEnabled(enabled);
		mBtnDisp.setEnabled(enabled);
		if (!bStarted)
			mBtnDisp.setEnabled(false);

		findViewById(R.id.btnNewSearch).setEnabled(enabled);
		findViewById(R.id.btnMoreSearch).setEnabled(enabled);

		if (!enabled) {
			findViewById(R.id.btnSource).setVisibility(View.GONE);
			findViewById(R.id.btnRefs).setVisibility(View.GONE);
		}
	}

	private void setWallpaper() {
		try {

			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
			int width = displayMetrics.widthPixels;
			int height = displayMetrics.heightPixels;

			if (width > height) { // landscape
				int tmp = width;
				width = height * 2;
				height = tmp;
			} else { // portrait
				width = width * 2;
			}

			Bitmap bitmap = null;

			if (!mSharedPref.getBoolean(PREF_WP_FULL_FILL, false)) {
				// to crop the image
				bitmap = mImageManager.getCurrentBitmap();

				Intent cropIntent = new Intent(this, CropImage.class);
				Bundle extras = new Bundle();
				extras.putBoolean("setWallpaper", true);
				extras.putInt("aspectX", width);
				extras.putInt("aspectY", height);
				extras.putInt("outputX", width);
				extras.putInt("outputY", height);
				extras.putBoolean("noFaceDetection", true);
				extras.putString("imgUrl", mImageManager.getCurrentStr());
				cropIntent.putExtras(extras);

				// ByteArrayOutputStream bs = new ByteArrayOutputStream();
				// bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs);
				// Log.d(TAG, "bitmap size is " + bs.size());
				// cropIntent.putExtra("data", bs.toByteArray());
				startActivity(cropIntent);

			} else {
				// to full fill the image
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inJustDecodeBounds = true;

				Bitmap bm = null;
				if (mImageManager.isCurrentAsset()) {
					bm = BitmapFactory.decodeStream(
							getAssets().open(mImageManager.getCurrentStr()),
							null, opt);
				} else {
					Log.d(TAG,
							"opening local bitmap "
									+ mImageManager.getCurrentStrLocal());
					FileInputStream fis = new FileInputStream(new File(
							mImageManager.getCurrentStrLocal()));
					bm = BitmapFactory.decodeStream(fis, null, opt);
				}
				int bm_w = opt.outWidth;
				int bm_h = opt.outHeight;
				Log.d(TAG, "origin: " + bm_w + "x" + bm_h);

				float ratio_hw = (float) bm_h / bm_w;
				Log.d(TAG, "bitmap original ratio height/width = " + ratio_hw);
				if (bm_w > width || bm_h > height) {
					if (height / ratio_hw <= width) {
						opt.outHeight = height;
						opt.outWidth = (int) (height / ratio_hw);
					} else {
						opt.outWidth = width;
						opt.outHeight = (int) (width * ratio_hw);
					}
				} else {
					if (height / ratio_hw <= width) {
						opt.outWidth = width;
						opt.outHeight = (int) (width * ratio_hw);
					} else {
						opt.outHeight = height;
						opt.outWidth = (int) (height / ratio_hw);
					}
				}
				Log.d(TAG, "scaled: " + opt.outWidth + "x" + opt.outHeight);

				opt.inJustDecodeBounds = false;
				float t = bm_w / ((float) width / 2);
				opt.inSampleSize = Math.round(t);

				Log.d(TAG, "inSampleSize = " + t + " => " + opt.inSampleSize);

				if (mImageManager.isCurrentAsset()) {
					bm = BitmapFactory.decodeStream(
							getAssets().open(mImageManager.getCurrentStr()),
							null, opt);
				} else {
					FileInputStream fis = new FileInputStream(new File(
							mImageManager.getCurrentStrLocal()));
					bm = BitmapFactory.decodeStream(fis, null, opt);
				}

				// bitmap = Bitmap.createBitmap(width, height,
				// Bitmap.Config.RGB_565);
				// Canvas canvas = new Canvas(bitmap);
				// canvas.drawBitmap(bm, (width - opt.outWidth) / 2,
				// (height - opt.outHeight) / 2, null);

				float f1 = (float) width / bm.getWidth();
				float f2 = (float) height / bm.getHeight();
				Matrix matrix = new Matrix();
				matrix.postScale(f1, f2);
				bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
						bm.getHeight(), matrix, true);
				bm.recycle();
				WallpaperManager.getInstance(this).setBitmap(bitmap);
			}

		} catch (IOException e) {
			Log.e(TAG, "Failed to set wallpaper!");
		}
	}
	private class MyClockGestureListener
			extends
				GestureDetector.SimpleOnGestureListener {
		private final int LARGE_MOVE = 0;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (e1.getX() - e2.getX() > LARGE_MOVE) {
				// Log.d(TAG, "ClockGesture Fling Left with velocity " +
				// velocityX);
				ChangeClockFace(1);
				return true;
			} else if (e2.getX() - e1.getX() > LARGE_MOVE) {
				// Log.d(TAG, "ClockGesture Fling Right with velocity " +
				// velocityX);
				ChangeClockFace(-1);
				return true;
			}

			return false;
		}

		private void ChangeClockFace(int step) {
			int face = (mFace + step + CLOCKS.length) % CLOCKS.length;
			if (mFace != face) {
				if (face < 0 || face >= CLOCKS.length) {
					mFace = 0;
				} else {
					mFace = face;
				}

				inflateClock();

				Editor edit = mSharedPref.edit();
				edit.putInt(PREF_CLOCK_FACE, mFace).commit();
			}
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (!getMLVisibility()) {
				setMLVisibility(true);
			}
			return true;
		}
	}

	private class MyGestureListener
			extends
				GestureDetector.SimpleOnGestureListener {
		private final int LARGE_MOVE = 0;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (mScaleType == ScaleType.CENTER) {
				return false;
			}

			if (mImageManager.isInitInProcess()) {
				return false;
			}

			if (mAnimationIndex == 1) { // slide horizontal
				if (e1.getX() - e2.getX() > LARGE_MOVE) {
					// Log.d(TAG, "Fling Left with velocity " + velocityX);
					goNextorPrev(1);
					return true;
				} else if (e2.getX() - e1.getX() > LARGE_MOVE) {
					// Log.d(TAG, "Fling Right with velocity " + velocityX);
					goNextorPrev(-1);
					return true;
				}
			} else if (mAnimationIndex == 2) { // slide vertical
				if (e1.getY() - e2.getY() > LARGE_MOVE) {
					// Log.d(TAG, "Fling Up with velocity " + velocityY);
					goNextorPrev(1);
					return true;
				} else if (e2.getY() - e1.getY() > LARGE_MOVE) {
					// Log.d(TAG, "Fling Down with velocity " + velocityY);
					goNextorPrev(-1);
					return true;
				}
			}

			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (mScaleType == ScaleType.CENTER) {
				DisplayMetrics displayMetrics = getResources()
						.getDisplayMetrics();
				int screen_width = displayMetrics.widthPixels;
				int screen_height = displayMetrics.heightPixels;

				ImageView iv = mImageViews[mImageViewCurrent];

				Rect rcBounds = iv.getDrawable().getBounds();
				int delta_w = (screen_width - rcBounds.width()) / 2;
				int delta_h = (screen_height - rcBounds.height()) / 2;

				Rect rc = new Rect();
				iv.getDrawingRect(rc);

				if ((rc.left + distanceX) > Math.min(-delta_w, delta_w)
						&& (rc.left + distanceX) < Math.max(-delta_w, delta_w)
						&& (rc.top + distanceY) > Math.min(-delta_h, delta_h)
						&& (rc.top + distanceY) < Math.max(-delta_h, delta_h)) {

				} else {
					if ((rc.left + distanceX) < Math.min(-delta_w, delta_w))
						distanceX = Math.min(-delta_w, delta_w) - rc.left;
					if ((rc.left + distanceX) > Math.max(-delta_w, delta_w))
						distanceX = Math.max(-delta_w, delta_w) - rc.left;
					if ((rc.top + distanceY) < Math.min(-delta_h, delta_h))
						distanceY = Math.min(-delta_h, delta_h) - rc.top;
					if ((rc.top + distanceY) > Math.max(-delta_h, delta_h))
						distanceY = Math.max(-delta_h, delta_h) - rc.top;
				}

				iv.scrollBy((int) distanceX, (int) distanceY);
			}
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			ImageView iv = mImageViews[mImageViewCurrent];
			if (mScaleType != DEFAULT_SCALETYPE) {
				mScaleType = DEFAULT_SCALETYPE;
				iv.setScaleType(mScaleType);
				iv.scrollTo(0, 0);
			} else if (mScaleType == DEFAULT_SCALETYPE) {
				if (bLargePicLoaded) {
					mScaleType = ScaleType.CENTER;
				} else {
					mScaleType = ALTER_SCALETYPE;
				}
				iv.setScaleType(mScaleType);

			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			if (!getMLVisibility()) {
				if (mCoverFlow.getVisibility() == View.GONE) {
					mCoverFlow
							.startAnimation(makeInAnimation(R.anim.slide_in_vertical));
					mCoverFlow.setVisibility(View.VISIBLE);
				} else {
					mCoverFlow.setVisibility(View.GONE);
					mCoverFlow
							.startAnimation(makeInAnimation(R.anim.slide_out_vertical_r));
				}
			}

			super.onLongPress(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (!getMLVisibility()) {
				setMLVisibility(true);
				if (mCoverFlow.getVisibility() == View.VISIBLE) {
					mCoverFlow.setVisibility(View.GONE);
					mCoverFlow
							.startAnimation(makeInAnimation(R.anim.slide_out_vertical_r));
				}
			} else {
				setMLVisibility(!getMLVisibility());
			}

			return super.onSingleTapConfirmed(e);
		}
	}

	@Override
	public boolean onKeyUp(int keycode, KeyEvent event) {
		switch (keycode) {

			case KeyEvent.KEYCODE_BACK :

				// new AlertDialog.Builder(this)
				// .setMessage(getString(R.string.exit_msg))
				// .setPositiveButton(getString(R.string.ok),
				// new DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog,
				// int which) {
				// finish();
				// }
				// })
				// .setNegativeButton(getString(R.string.cancel),
				// new DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog,
				// int whichButton) {
				// }
				// }).create().show();

				View v = findViewById(R.id.webView);
				if (v.getVisibility() == View.VISIBLE) {
					v.setVisibility(View.GONE);
					ScaleInAnimation(v);
					return false;
				}

				if (!getMLVisibility()) {
					setMLVisibility(true);
					return false;
				}

				if (!bKeyBackIn2Sec) {
					Toast.makeText(this, getString(R.string.exit_toast),
							Toast.LENGTH_SHORT).show();
					bKeyBackIn2Sec = true;
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							bKeyBackIn2Sec = false;
						}

					}, 2000); // reset BACK status in 2 seconds

				} else {
					finish();
				}

				return false;

			case KeyEvent.KEYCODE_DPAD_LEFT :
			case KeyEvent.KEYCODE_DPAD_UP :
				if (bStarted && !mImageManager.isInitInProcess())
					goNextorPrev(-1);
				break;

			case KeyEvent.KEYCODE_DPAD_RIGHT :
			case KeyEvent.KEYCODE_DPAD_DOWN :
			case KeyEvent.KEYCODE_SPACE :
			case KeyEvent.KEYCODE_ENTER :
				if (!mImageManager.isInitInProcess())
					goNextorPrev(1);
				break;

			default :
		}

		return super.onKeyUp(keycode, event);
	}

	protected void inflateClock() {
		if (mClock != null) {
			mClockLayout.removeView(mClock);
		}

		LayoutInflater.from(this).inflate(CLOCKS[mFace], mClockLayout);
		mClock = findViewById(R.id.clock);
	}

	private Animation makeInAnimation(int id) {
		Animation inAnimation = AnimationUtils.loadAnimation(this, id);
		return inAnimation;
	}

	private Animation makeOutAnimation(int id) {
		Animation outAnimation = AnimationUtils.loadAnimation(this, id);
		return outAnimation;
	}

	private void ScaleOutAnimation(View view) {
		ScaleAnimation myAnimation_Scale = new ScaleAnimation(0.1f, 1.0f, 0.1f,
				1f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		myAnimation_Scale.setInterpolator(new AccelerateInterpolator());
		AnimationSet aa = new AnimationSet(true);
		aa.addAnimation(myAnimation_Scale);
		aa.setDuration(300);

		view.startAnimation(aa);
	}

	private void ScaleInAnimation(View view) {
		ScaleAnimation myAnimation_Scale = new ScaleAnimation(1.0f, 0.0f, 1.0f,
				0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		myAnimation_Scale.setInterpolator(new AccelerateInterpolator());
		AnimationSet aa = new AnimationSet(true);
		aa.addAnimation(myAnimation_Scale);
		aa.setDuration(300);

		view.startAnimation(aa);
	}

	private class ButtonStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean enabled = intent.getBooleanExtra("buttonState", true);
			EnableNextPrevButtons(enabled);
		}
	}

	static int total = 0;
	static int pages = 0;
	static int key = -1;
	static int page = -1;
	static int items = 0;
	static int item = -1;

	private class ProgressBarReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS)
				return;

			int progress1 = intent.getIntExtra("progress", -1);
			int progress2 = intent.getIntExtra("progress2", -1);
			int progress3 = intent.getIntExtra("progress3", -1);
			int pos = intent.getIntExtra("fileId",
					ImageManager.INVALID_PIC_INDEX);
			// Log.d(TAG, pos + ") prg1: " + progress1 + ", prg2 = " + progress2
			// + ", prg3 = " + progress3);

			int tmp = intent.getIntExtra("prg_total", -1);
			if (tmp >= 0)
				total = tmp;
			if ((tmp = intent.getIntExtra("prg_key", -1)) != -1) {
				key = tmp;
				page = 0;
			}
			if ((tmp = intent.getIntExtra("prg_pages", 0)) > 0)
				pages = tmp;
			if ((tmp = intent.getIntExtra("prg_page", -1)) != -1) {
				page = tmp;
				item = 0;
			}
			if ((tmp = intent.getIntExtra("prg_items", 0)) > 0)
				items = tmp;
			if ((tmp = intent.getIntExtra("prg_item", -1)) != -1)
				item = tmp;
			if (pages != 0 && total != 0 && items != 0) {
				int i = 100 * (key * pages + page) / pages;
				int j = 100 * item / items / total;
				mProgressBar.setVisibility(View.VISIBLE);
				mProgressBar.setProgress(i + j);
			}

			if (progress1 != 0 && progress1 != -1) {
				mProgressBar.setVisibility(View.VISIBLE);
				mProgressBar.setProgress(progress1);
			}
			if (progress2 != 0 && progress2 != -1) {

				int current = mImageManager.getCurrent();
				if (current != ImageManager.INVALID_PIC_INDEX
						&& current < mImageManager.mImageList.size()
						&& !mImageManager.mImageList.get(current).isCached()) {
					mProgressBar2.setVisibility(View.VISIBLE);
				} else {
					mProgressBar2.setVisibility(View.GONE);
				}
			}

			if (pos != ImageManager.INVALID_PIC_INDEX
					&& pos == mImageManager.getCurrent()) {

				mProgressBar2.setSecondaryProgress(progress2);

				TextView tv;
				if (Constants.DEBUG) {
					tv = (TextView) findViewById(R.id.picName);
					String tmpString = tv.getText().toString();
					if (tmpString.contains("\n"))
						tmpString = tmpString.substring(
								tmpString.lastIndexOf("\n") + 1,
								tmpString.length());
					if (tmpString.contains("%"))
						tmpString = tmpString.substring(0,
								tmpString.lastIndexOf(" "));

					if (progress2 != -1 && progress2 != 100) {
						tmpString += String.format(" %d%%", progress2);
					}

					tv.setText(WatchApp.getDownloadManager().getDownloadsInfo()
							+ tmpString);
				}

				if (progress2 == 100) {
					int width = 0, height = 0;
					Bitmap bitmap = mImageManager.getPosBitmap(pos, false);
					if (bitmap != null) {
						width = bitmap.getWidth();
						height = bitmap.getHeight();
						DisplayMetrics displayMetrics = getResources()
								.getDisplayMetrics();
						if (width > displayMetrics.widthPixels
								|| height > displayMetrics.heightPixels)
							bLargePicLoaded = true;
						else
							bLargePicLoaded = false;
					}

					if (Constants.DEBUG) {
						tv = (TextView) findViewById(R.id.picName);
						tv.setText(WatchApp.getDownloadManager()
								.getDownloadsInfo()
								+ String.format("%d/%d, %dx%d",
										mImageManager.getCurrent() + 1,
										mImageManager.getImageListSize(),
										width, height));
					}

					mImageViews[mImageViewCurrent].setScaleType(mScaleType);
					mImageViews[mImageViewCurrent].setImageBitmap(bitmap);
					mImageManager.mImageList.get(mImageManager.getCurrent())
							.setCached(true);
				}

				if (progress3 == 100) {
					Bitmap bitmap = mImageManager.getPosBitmap(pos, true);
					mImageViews[mImageViewCurrent].setImageBitmap(bitmap);
				}
			}

			if (progress1 == 0 || progress1 == 100) {
				// mProgressBar.setVisibility(View.GONE);
				mProgressBar.setProgress(0);
			}

			if (progress2 == 0 || progress2 == 100) {
				mProgressBar2.setVisibility(View.GONE);
				mProgressBar2.setSecondaryProgress(0);
				mProgressIcon.setVisibility(View.GONE);
			} else if (progress2 != -1) {
				mProgressIcon.setVisibility(View.VISIBLE);
			}

			if (pos != ImageManager.INVALID_PIC_INDEX && progress3 == 100) {
				ImageAdapter imageAdapter = (ImageAdapter) mCoverFlow
						.getAdapter();
				if (imageAdapter != null) {
					SoftReference<ImageView> imgView = imageAdapter.mCached
							.get(pos);
					if (imgView != null && imgView.get() != null) {
						imgView.get().setImageBitmap(
								mImageManager.getPosBitmap(pos, true));
						imageAdapter.mCached.remove(pos);
						imageAdapter.mCached.put(pos, imgView);
						imageAdapter.notifyDataSetChanged();
					}
				}
			}
		}
	}
}
