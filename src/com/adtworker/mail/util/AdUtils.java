package com.adtworker.mail.util;

import android.app.Activity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.adtworker.mail.constants.Constants;
import com.adview.AdViewInterface;
import com.adview.AdViewLayout;
import com.adview.AdViewTargeting;
import com.adview.AdViewTargeting.RunMode;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class AdUtils {

	/**
	 * 设置Adview广告位
	 * 
	 * @param context
	 * @param parent
	 * @param setAdInterface
	 *            是否重写AD接口
	 * @return void
	 */
	public static void setupAdLayout(Activity context, ViewGroup parent,
			boolean setAdInterface) {
		if (android.os.Build.VERSION.SDK_INT < 12 || Constants.ALWAYS_SHOW_AD) {

			/* 下面两行只用于测试,完成后一定要去掉,参考文挡说明 */
			// AdViewTargeting.setUpdateMode(UpdateMode.EVERYTIME); //
			// 保证每次都从服务器取配置
			AdViewTargeting.setRunMode(RunMode.NORMAL); // 保证所有选中的广告公司都为测试状态
			/* 下面这句方便开发者进行发布渠道统计,详细调用可以参考java doc */
			// AdViewTargeting.setChannel(Channel.GOOGLEMARKET);

			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.TOP | Gravity.CENTER;

			AdViewLayout adViewLayout = new AdViewLayout(context,
					"SDK20122309480217x9sp4og4fxrj2ur");
			if (setAdInterface) {
				adViewLayout.setAdViewInterface((AdViewInterface) context);
			}
			parent.addView(adViewLayout);
			parent.invalidate();
		} else if (android.os.Build.VERSION.SDK_INT >= 12) {
			setupAdmobAdView(context, parent);
		}
	}

	/**
	 * 设置Admob广告位
	 * 
	 * @param context
	 * @param parent
	 * @return void
	 */
	public static void setupAdmobAdView(Activity context, ViewGroup parent) {
		AdView adView = new AdView(context, AdSize.BANNER, "a14fab3d9421605");
		parent.addView(adView);
		adView.loadAd(new AdRequest());
	}

	/**
	 * 设置suizong广告位
	 * 
	 * @param context
	 * @param parent
	 * @return void
	 */
	public static void setupSuizongAdView(Activity context, ViewGroup parent) {
		if (android.os.Build.VERSION.SDK_INT < 12 /* || Constants.ALWAYS_SHOW_AD */) {
			com.suizong.mobplate.ads.AdView adView = new com.suizong.mobplate.ads.AdView(
					context, com.suizong.mobplate.ads.AdSize.BANNER,
					"4f46e9bc7c6e1848b8d48e61");
			parent.addView(adView);
			com.suizong.mobplate.ads.AdRequest adRequest = new com.suizong.mobplate.ads.AdRequest();
			adView.loadAd(adRequest);
		}
	}

}
