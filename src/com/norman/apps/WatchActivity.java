package com.norman.apps;

import java.util.Random;

import com.mt.airad.AirAD;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AnalogClock;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class WatchActivity extends Activity {
	static {
        AirAD.setGlobalParameter("APPID", true);
    }
	private AirAD ad;
    private boolean bLayoutHidden = false;
    private boolean bFirstLaunch  = true;
    
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
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);     
        setContentView(R.layout.main);
        
        // Adding airAD
        LinearLayout layout= (LinearLayout) findViewById(R.id.adLayout);
        ad = new AirAD(this);
        layout.addView(ad);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	// Navigation buttons
        TextView buttonPrev = (TextView) findViewById(R.id.btnPrev);
        TextView buttonNext = (TextView) findViewById(R.id.btnNext);
        TextView buttonDisp = (TextView) findViewById(R.id.btnDisp);
        ImageView buttonImg = (ImageView)findViewById(R.id.picView);
        
        buttonPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ImageView img = (ImageView)findViewById(R.id.picView);
				Random r = new Random();
				int iPicRes = (r.nextInt(PICS.length));
        		Log.d("iWatch", "Showing previous picture id " + iPicRes);      		
        		img.setImageResource(PICS[iPicRes]);
			}
        });
        
        buttonNext.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View arg0) {
        		if (bFirstLaunch) {
        			((AnalogClock)findViewById(R.id.analogClock)).setVisibility(View.GONE);
        			((TextView)findViewById(R.id.btnPrev)).setEnabled(true);
        			bFirstLaunch = false;
        		}
        		
        		ImageView img = (ImageView)findViewById(R.id.picView);
        		Random r = new Random();
        		int iPicRes = (r.nextInt(PICS.length));
        		Log.d("iWatch", "Showing next picture id " + iPicRes);        		
        		img.setImageResource(PICS[iPicRes]);
			}
        });
        
        buttonDisp.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View arg0) {
        		// hide mainLayout only leave background image
        		LinearLayout mainLayout = (LinearLayout)findViewById(R.id.mainLayout);
        		bLayoutHidden = mainLayout.getVisibility() != View.VISIBLE;
				if (!bLayoutHidden) {
					mainLayout.setVisibility(View.GONE);
				}
			}
        });
        
        buttonImg.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View arg0) {
        		LinearLayout mainLayout = (LinearLayout)findViewById(R.id.mainLayout);
        		bLayoutHidden = mainLayout.getVisibility() != View.VISIBLE;
				if (bLayoutHidden) {
					mainLayout.setVisibility(View.VISIBLE);
				}
			}
        });
    }
    
}
