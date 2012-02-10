package com.norman.apps;

import java.io.IOException;
import java.util.Random;
import java.util.Stack;

import com.mt.airad.AirAD;

import android.R.integer;
import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WatchActivity extends Activity {
	static {
        AirAD.setGlobalParameter("d7198ef2-3ec8-4713-a4c0-64cb738bdd6a", true);
    }
	
	private AirAD ad;
	private ImageView mImageView;
	private TextView  mBtnPrev;
	private TextView  mBtnNext;
	private TextView  mBtnDisp;
	
    private final String TAG = "iWatch";
    private final int INVALID_PIC_INDEX= -1;
    private final Random mRandom = new Random(System.currentTimeMillis());
    
    private int iPicIndex = INVALID_PIC_INDEX;
    private Stack<Integer> sPicHistory = new Stack<Integer>();
    GestureDetector mGestureDetector;
    
    final static int[] PICS = {
    	R.drawable.rosimm001,
    	R.drawable.rosimm002,
    	R.drawable.rosimm003,
    	R.drawable.rosimm004,
    	R.drawable.rosimm005,
    	R.drawable.rosimm006,
    	R.drawable.rosimm007,
    	R.drawable.rosimm008,
    	R.drawable.rosimm009,
    	R.drawable.rosimm010,
    	R.drawable.rosimm011,
    	R.drawable.rosimm012,
    	R.drawable.rosimm013,
    	R.drawable.rosimm014,
    	R.drawable.rosimm015,
    	R.drawable.rosimm016,
    	R.drawable.rosimm017,
    	R.drawable.rosimm018
    };
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {	
    	Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);     
        setContentView(R.layout.main);
        mImageView = (ImageView)findViewById(R.id.picView);
        mBtnPrev = (TextView)findViewById(R.id.btnPrev);
        mBtnNext = (TextView)findViewById(R.id.btnNext);
        mBtnDisp = (TextView)findViewById(R.id.btnDisp);
        
        if (sPicHistory.empty()) {
        	mBtnPrev.setEnabled(false);
        }
        
        // Adding airAD
        LinearLayout layout= (LinearLayout) findViewById(R.id.adLayout);
        ad = new AirAD(this);
        layout.addView(ad);
        
        setupButtons();
        
        mGestureDetector = new GestureDetector(this, new MyGestureListener());
        OnTouchListener rootListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        };
        mImageView.setOnTouchListener(rootListener);
    }
    
    @Override
    public void onStart() {
    	Log.d(TAG, "onStart()");
    	super.onStart();
    }
    
    @Override
    public void onResume() {
    	Log.d(TAG, "onResume()");
    	super.onResume();
    }
    
    @Override
    public void onPause() {
    	Log.d(TAG, "onPause()");
    	super.onPause();
    }
    
    @Override
    public void onStop() {
    	Log.d(TAG, "onStop()");
    	super.onStop();
    }
    
    @Override
    public void onDestroy() {
    	Log.d(TAG, "onDestroy()");
    	super.onDestroy();
    }
    
    private void setupButtons() {
        
        mBtnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				iPicIndex = sPicHistory.pop();
				Log.d(TAG, "Showing previous picture id " + iPicIndex);
				
				mImageView.setImageResource(PICS[iPicIndex]);
				
				if (sPicHistory.empty()) {
					mBtnPrev.setEnabled(false);
					mBtnNext.setText(getResources().getString(R.string.start));
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
        			tmpIndex = mRandom.nextInt(PICS.length);
        		} while (tmpIndex == iPicIndex);
        		
        		iPicIndex = tmpIndex;
        		Log.d(TAG, "Showing new picture id " + iPicIndex);

        		mImageView.setImageResource(PICS[iPicIndex]);

        		if (!sPicHistory.empty()) {
        			Log.d(TAG, "Picture stack: " + sPicHistory);
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
    			getClockVisibility() ? R.string.hide_clock : R.string.show_clock);
    	menu.findItem(R.id.menu_settings).setEnabled(false);
    	
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
    	case R.id.menu_toggle_clock:
    		setClockVisibility(!getClockVisibility());
    		break;
    	
    	case R.id.menu_settings:
    		//startActivity(new Intent(this, SettingsActivity.class));
    		break;

    	case R.id.menu_set_wallpaper:
    		Thread thd = new Thread(new Runnable() {
    			public void run() {
    				setWallpaper();
    			}
    		});
    		thd.start();

    		Toast tst = Toast.makeText(this, "", Toast.LENGTH_LONG);
    		ImageView view = new ImageView(this);
    		view.setImageResource(R.drawable.ic_launcher_alarmclock);
    		tst.setView(view);
    		tst.setGravity(Gravity.CENTER, tst.getXOffset()/2, tst.getYOffset()/2);
    		tst.show();

			break;
    		
    	default:
    		
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
    		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), PICS[iPicIndex]);
    		Bitmap corppedBitmap = Bitmap.createBitmap(displayMetrics.widthPixels * 2,
    				displayMetrics.heightPixels, Bitmap.Config.RGB_565);
    		
    		Canvas canvas = new Canvas(corppedBitmap);
    		Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    		Rect dstRect = new Rect(0, 0, displayMetrics.widthPixels * 2, displayMetrics.heightPixels);
    		int dx = (srcRect.width()  - dstRect.width())  / 2;
    		int dy = (srcRect.height() - dstRect.height()) / 2;
    		srcRect.inset(Math.max(0, dx),  Math.max(0,  dy));
    		srcRect.inset(Math.max(0, -dx), Math.max(0, -dy));
    		canvas.drawBitmap(bitmap, srcRect, dstRect, null);
    		
    		WallpaperManager.getInstance(this).setBitmap(corppedBitmap);
    		Log.d(TAG, "Set picture " + iPicIndex + " as wallpaper.");
    	
    	} catch (IOException e) {
    		Log.e(TAG, "Failed to set wallpaper!");
    	}
    }
    
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
    	@Override
    	public boolean onScroll(MotionEvent e1, MotionEvent e2,
    			float distanceX, float distanceY) {
    		if (mImageView.getScaleType() == ScaleType.CENTER_CROP) {
    			mImageView.scrollBy((int)distanceX, (int)distanceY);
    		}
    		return true;
    	}
    	
    	@Override
    	public boolean onDoubleTap(MotionEvent e) {
    		if (mImageView.getScaleType() == ScaleType.CENTER_CROP) {
    			mImageView.setScaleType(ScaleType.FIT_CENTER);
    			mImageView.scrollTo(0, 0);
    		} else {
    			mImageView.setScaleType(ScaleType.CENTER_CROP);
    		}
    		return true;
    	}
    	
    	@Override
    	public boolean onSingleTapConfirmed(MotionEvent e) {
    		if (!getMLVisibility()) {
    			setMLVisibility(true);
    		}
            return true;
        }
    }
}