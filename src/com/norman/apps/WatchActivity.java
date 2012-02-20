package com.norman.apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adview.AdViewInterface;
import com.adview.AdViewLayout;
import com.adview.AdViewTargeting;
import com.adview.AdViewTargeting.RunMode;

public class WatchActivity extends Activity implements AdViewInterface {

	private final Handler mHandler = new Handler();
	private ImageView mImageView;
	private TextView mBtnPrev;
	private TextView mBtnNext;
	private TextView mBtnDisp;
	private ViewGroup mClockLayout;
	private View mClock = null;

	private final String TAG = "WatchActivity";
	private final String ASSETS_NAME = "pics.zip";
	public final static int INVALID_PIC_INDEX = -1;
	public final static String APP_FOLDER = "/data/com.norman.apps";
	public final static String PIC_FOLDER = "/iWatch";
	private final Random mRandom = new Random(System.currentTimeMillis());

	private int iPicIndex = INVALID_PIC_INDEX;
	private int mFace = -1;
	private boolean bKeyBackIn2Sec = false;
	private boolean bLargePicLoaded = false;
	private final Stack<Integer> sPicHistory = new Stack<Integer>();
	private GestureDetector mGestureDetector;
	private ProgressDialog mProcessDialog;
	private SharedPreferences mSharedPref;

	final static String PREFERENCES = "iWatch";
	final static String PREF_CLOCK_FACE = "face";
	final static String PREF_PIC_CODE = "pic_code";

	// 采用反射运行时动态读取图片，在res/raw文件目录下按数组创建对应文件名
	private final static ArrayList<String> PICS = new ArrayList<String>();
	private final static int[] CLOCKS = {R.layout.clock_no_dial,
			R.layout.clock_no_dial2, R.layout.clock_no_dial3,
			R.layout.clock_no_dial4, R.layout.clock_appwidget,
			R.layout.clock_appwidget1, R.layout.clock_appwidget3,
			R.layout.clock_basic_bw, R.layout.clock_basic_bw1,
			R.layout.clock_basic_bw3, R.layout.clock_googly,
			R.layout.clock_googly1, R.layout.clock_googly3,
			R.layout.clock_droid2, R.layout.clock_droid2_1,
			R.layout.clock_droid2_2, R.layout.clock_droid2_3,
			R.layout.clock_droids, R.layout.clock_droids1,
			R.layout.clock_droids2, R.layout.clock_droids3,
			R.layout.digital_clock};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mImageView = (ImageView) findViewById(R.id.picView);
		mBtnPrev = (TextView) findViewById(R.id.btnPrev);
		mBtnNext = (TextView) findViewById(R.id.btnNext);
		mBtnDisp = (TextView) findViewById(R.id.btnDisp);
		mSharedPref = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		mClockLayout = (ViewGroup) findViewById(R.id.clockLayout);
		mClockLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int face = (mFace + 1) % CLOCKS.length;
				if (mFace != face) {
					Log.d(TAG, "clock face ID is " + face);
					if (face < 0 || face >= CLOCKS.length)
						mFace = 0;
					else
						mFace = face;
					inflateClock();
					Editor edit = mSharedPref.edit();
					edit.putInt(PREF_CLOCK_FACE, mFace).commit();
				}
			}
		});

		LinearLayout layout = (LinearLayout) findViewById(R.id.adLayout);
		if (layout == null) {
			return;
		}

		/* 下面两行只用于测试,完成后一定要去掉,参考文挡说明 */
		// AdViewTargeting.setUpdateMode(UpdateMode.EVERYTIME); //
		// 保证每次都从服务器取配置
		AdViewTargeting.setRunMode(RunMode.NORMAL); // 保证所有选中的广告公司都为测试状态
		/* 下面这句方便开发者进行发布渠道统计,详细调用可以参考java doc */
		// AdViewTargeting.setChannel(Channel.GOOGLEMARKET);
		AdViewLayout adViewLayout = new AdViewLayout(this,
				"SDK20122309480217x9sp4og4fxrj2ur");
		adViewLayout.setAdViewInterface(this);
		layout.addView(adViewLayout);
		layout.invalidate();

		setupButtons();
		initPicsList();

		mGestureDetector = new GestureDetector(this, new MyGestureListener());
		OnTouchListener rootListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mGestureDetector.onTouchEvent(event);
				return true;
			}
		};
		mImageView.setOnTouchListener(rootListener);

		if (sPicHistory.empty()) {
			mBtnPrev.setEnabled(false);
		}
	}
	private final Runnable mUpdateKeyBackState = new Runnable() {
		@Override
		public void run() {
			bKeyBackIn2Sec = false;
			mHandler.removeCallbacks(mUpdateKeyBackState);
		}
	};

	private final Runnable mUnzipTask = new Runnable() {
		@Override
		public void run() {

			if (!new File(getPicPath()).exists())
				unzipFile(getPicPath(), ASSETS_NAME, true);

			initPicsList();

			mHandler.removeCallbacks(mUnzipTask);
		}

	};

	private final Runnable mUpdateImageView = new Runnable() {
		@Override
		public void run() {
			try {
				InputStream is = getAssets().open(PICS.get(iPicIndex));
				Bitmap bm = BitmapFactory.decodeStream(is);
				mImageView.setImageBitmap(bm);

				DisplayMetrics displayMetrics = getResources()
						.getDisplayMetrics();
				int width = displayMetrics.widthPixels;
				int height = displayMetrics.heightPixels;
				if (bm.getWidth() > displayMetrics.widthPixels
						|| bm.getHeight() > displayMetrics.heightPixels)
					bLargePicLoaded = true;
				else
					bLargePicLoaded = false;

			} catch (Exception e) {
				e.printStackTrace();

			} finally {
				mHandler.removeCallbacks(mUpdateImageView);
			}
		}
	};

	public String getPicPath() {
		return Environment.getDataDirectory() + APP_FOLDER + PIC_FOLDER;
		// return Environment.getExternalStorageDirectory() + PIC_FOLDER;
	}

	@Override
	public void onStart() {
		// Log.d(TAG, "onStart()");
		super.onStart();
	}

	@Override
	public void onResume() {
		// Log.d(TAG, "onResume()");
		super.onResume();

		int face = mSharedPref.getInt(PREF_CLOCK_FACE, 0);
		if (mFace != face) {
			if (face < 0 || face >= CLOCKS.length)
				mFace = 0;
			else
				mFace = face;
			inflateClock();
		}
	}

	@Override
	public void onPause() {
		// Log.d(TAG, "onPause()");
		super.onPause();
	}

	@Override
	public void onStop() {
		// Log.d(TAG, "onStop()");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}

	private void setupButtons() {

		mBtnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				iPicIndex = sPicHistory.pop();
				mHandler.post(mUpdateImageView);

				if (sPicHistory.empty()) {
					mBtnPrev.setEnabled(false);
					Editor edit = mSharedPref.edit();
					edit.remove("CurPicIndex").commit();
				}
			}
		});

		mBtnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (iPicIndex == INVALID_PIC_INDEX) {
					setClockVisibility(false);
					mBtnNext.setText(getResources().getString(R.string.strNext));
				} else {
					sPicHistory.push(iPicIndex);
				}

				int tmpIndex = INVALID_PIC_INDEX;
				do {
					tmpIndex = mRandom.nextInt(PICS.size());
				} while (tmpIndex == iPicIndex);

				iPicIndex = tmpIndex;

				mHandler.post(mUpdateImageView);

				if (!sPicHistory.empty()) {
					mBtnPrev.setEnabled(true);
				}

			}
		});

		mBtnDisp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// hide mainLayout only leave background image
				if (getMLVisibility()) {
					setMLVisibility(false);
					getWindow().setFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);

				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_toggle_clock).setTitle(
				getClockVisibility()
						? R.string.hide_clock
						: R.string.show_clock);

		// Hide settings in current version
		menu.findItem(R.id.menu_settings).setVisible(false);

		if (iPicIndex == INVALID_PIC_INDEX) {
			menu.findItem(R.id.menu_set_wallpaper).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_set_wallpaper).setEnabled(true);
		}

		return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_toggle_clock :
				setClockVisibility(!getClockVisibility());
				break;

			case R.id.menu_settings :
				startActivity(new Intent(this, Settings.class));
				break;

			case R.id.menu_set_livewallpaper :
				Editor myEdit = mSharedPref.edit();
				if (iPicIndex != INVALID_PIC_INDEX) {
					myEdit.putString(PREF_PIC_CODE, PICS.get(iPicIndex));
				} else {
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
	}

	private boolean getMLVisibility() {
		return getLayoutVisibility(R.id.mainLayout);
	}

	private void setMLVisibility(boolean bVisibility) {
		setLayoutVisibility(R.id.mainLayout, bVisibility);
	}

	private void setWallpaper() {
		try {
			WallpaperManager.getInstance(this).setBitmap(
					((BitmapDrawable) mImageView.getDrawable()).getBitmap());

		} catch (IOException e) {
			Log.e(TAG, "Failed to set wallpaper!");
		}
	}
	private class MyGestureListener
			extends
				GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (mImageView.getScaleType() == ScaleType.CENTER) {
				mImageView.scrollBy((int) distanceX, (int) distanceY);
			}
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mImageView.getScaleType() != ScaleType.CENTER_INSIDE) {
				mImageView.setScaleType(ScaleType.CENTER_INSIDE);
				mImageView.scrollTo(0, 0);
			} else if (mImageView.getScaleType() == ScaleType.CENTER_INSIDE) {
				if (bLargePicLoaded)
					mImageView.setScaleType(ScaleType.CENTER);
				else
					mImageView.setScaleType(ScaleType.FIT_CENTER);

			}

			return true;
		}
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (!getMLVisibility()) {
				setMLVisibility(true);
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_FULLSCREEN);

			}
			return true;
		}
	}

	@Override
	public void onClickAd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisplayAd() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onKeyUp(int keycode, KeyEvent event) {
		switch (keycode) {

			case KeyEvent.KEYCODE_BACK :
				// alert user when key_back is pressed
				// new AlertDialog.Builder(this)
				// .setMessage(getString(R.string.exit_msg))
				// .setPositiveButton(getString(R.string.ok),
				// new DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog,
				// int whichButton) {
				// finish();
				// }
				// })
				// .setNegativeButton(getString(R.string.cancel),
				// new DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog,
				// int whichButton) {
				//
				// }
				// }).create().show();

				if (!bKeyBackIn2Sec) {
					Toast.makeText(this, getString(R.string.exit_toast),
							Toast.LENGTH_SHORT).show();
					bKeyBackIn2Sec = true;
					mHandler.postDelayed(mUpdateKeyBackState, 2000);
				} else {
					finish();
				}

				return false;
		}

		return super.onKeyUp(keycode, event);
	}

	private void unzipFile(String targetPath, String zipFilePath,
			boolean isAssets) {

		try {
			File zipFile = new File(zipFilePath);
			InputStream is;
			if (isAssets) {
				is = getAssets().open(zipFilePath);
			} else {
				is = new FileInputStream(zipFile);
			}
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				String zipPath = entry.getName();
				try {

					if (entry.isDirectory()) {
						File zipFolder = new File(targetPath + File.separator
								+ zipPath);
						if (!zipFolder.exists()) {
							zipFolder.mkdirs();
						}
					} else {
						File file = new File(targetPath + File.separator
								+ zipPath);
						if (!file.exists()) {
							File pathDir = file.getParentFile();
							pathDir.mkdirs();
							file.createNewFile();
						}

						FileOutputStream fos = new FileOutputStream(file);
						int bread;
						while ((bread = zis.read()) != -1) {
							fos.write(bread);
						}
						fos.close();

					}

				} catch (Exception e) {
					continue;
				}
			}
			zis.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private boolean isValidPic(File file) {
		if (file.isFile()) {
			String fileName = file.getName().toLowerCase();
			return isValidPic(fileName);
		}
		return false;
	}

	private boolean isValidPic(String filename) {
		if (filename.endsWith(".jpg") || filename.endsWith(".png")
				|| filename.endsWith(".bmp")) {
			return true;
		}
		return false;
	}

	private void initPicsList() {
		ArrayList<String> arrayList = getAssetsPicsList("pics");
		for (int i = 0; i < arrayList.size(); i++)
			PICS.add(arrayList.get(i));
		// Log.d(TAG, "PICS: " + PICS);
	}

	private ArrayList<String> getPicsList(String path) {
		ArrayList<String> arrayList = new ArrayList<String>();
		File[] files = new File(path).listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				ArrayList<String> tmpArrayList = getPicsList(path
						+ File.separator + file.getName());
				for (int i = 0; i < tmpArrayList.size(); i++) {
					arrayList.add(file.getName() + File.separator
							+ tmpArrayList.get(i));
				}
			}
			if (isValidPic(file)) {
				arrayList.add(file.getName());
			}
		}
		return arrayList;
	}

	private ArrayList<String> getAssetsPicsList(String path) {
		if (path == null)
			path = "";

		ArrayList<String> arrayList = new ArrayList<String>();
		try {
			String[] filenames = getAssets().list(path);
			for (int i = 0; i < filenames.length; i++) {
				String filepath = path + File.separator + filenames[i];
				if (isValidPic(filepath))
					arrayList.add(filepath);
				else {
					ArrayList<String> tmp = getAssetsPicsList(filepath);
					for (int j = 0; j < tmp.size(); j++)
						arrayList.add(tmp.get(j));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return arrayList;
	}

	protected void inflateClock() {
		if (mClock != null) {
			mClockLayout.removeView(mClock);
		}

		LayoutInflater.from(this).inflate(CLOCKS[mFace], mClockLayout);
		mClock = findViewById(R.id.clock);
	}
}
