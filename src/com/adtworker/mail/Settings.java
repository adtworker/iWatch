package com.adtworker.mail;

import java.sql.Time;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.ViewGroup;

import com.adview.AdViewLayout;
import com.adview.AdViewTargeting;
import com.adview.AdViewTargeting.RunMode;

public class Settings extends PreferenceActivity
		implements
			Preference.OnPreferenceChangeListener {

	final static String TAG = "Settings";
	SharedPreferences mSharedPref;

	// private CheckBoxPreference mOrientation;
	private CheckBoxPreference mAutoHideClock;
	private CheckBoxPreference mAutoHideAD;
	private CheckBoxPreference mBossKey;
	private CheckBoxPreference mFullFill;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		setContentView(R.layout.pref_adview);
		setupAdLayout();

		mSharedPref = getSharedPreferences(WatchActivity.PREFERENCES,
				Context.MODE_PRIVATE);
		mAutoHideClock = (CheckBoxPreference) findPreference(WatchActivity.PREF_AUTOHIDE_CLOCK);
		mAutoHideAD = (CheckBoxPreference) findPreference(WatchActivity.PREF_AUTOHIDE_AD);
		mAutoHideAD.setEnabled(false);

		mBossKey = (CheckBoxPreference) findPreference(WatchActivity.PREF_BOSS_KEY);
		mFullFill = (CheckBoxPreference) findPreference(WatchActivity.PREF_FULL_FILL);
	}
	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference preference) {
		Editor ed = mSharedPref.edit();
		if (WatchActivity.PREF_AUTOHIDE_CLOCK.equals(preference.getKey())) {
			ed.putBoolean(WatchActivity.PREF_AUTOHIDE_CLOCK,
					mAutoHideClock.isChecked()).commit();
			return true;
		}

		if (WatchActivity.PREF_AUTOHIDE_AD.equals(preference.getKey())) {
			ed.putBoolean(WatchActivity.PREF_AUTOHIDE_AD,
					mAutoHideAD.isChecked()).commit();

			update_ad_sum();

			return true;
		}

		if (WatchActivity.PREF_BOSS_KEY.equals(preference.getKey())) {
			ed.putBoolean(WatchActivity.PREF_BOSS_KEY, mBossKey.isChecked())
					.commit();
			return true;
		}

		if (WatchActivity.PREF_FULL_FILL.equals(preference.getKey())) {
			ed.putBoolean(WatchActivity.PREF_FULL_FILL, mFullFill.isChecked())
					.commit();
			return true;
		}

		return super.onPreferenceTreeClick(prefScreen, preference);
	}

	protected void refresh() {
		mAutoHideClock.setChecked(mSharedPref.getBoolean(
				WatchActivity.PREF_AUTOHIDE_CLOCK, true));

		mAutoHideAD.setChecked(mSharedPref.getBoolean(
				WatchActivity.PREF_AUTOHIDE_AD, false));

		mBossKey.setChecked(mSharedPref.getBoolean(WatchActivity.PREF_BOSS_KEY,
				false));

		mFullFill.setChecked(mSharedPref.getBoolean(
				WatchActivity.PREF_FULL_FILL, false));

		update_ad_sum();
	}

	protected void update_ad_sum() {
		if (mAutoHideAD.isChecked()) {
			String timeStr = mSharedPref.getString(
					WatchActivity.PREF_AD_CLICK_TIME, "");
			if (timeStr.length() != 0) {
				Time time = new Time(System.currentTimeMillis());
				Time time2Cmp = new Time(time.getHours() - 1,
						time.getMinutes(), time.getSeconds());
				Time timeClick = Time.valueOf(timeStr);

				if (timeClick.after(time2Cmp)) {
					mAutoHideAD
							.setSummary(getString(R.string.pref_autohide_ad_sum)
									+ getString(R.string.last_click_time)
									+ timeClick.toString());
					return;
				} else {
					Log.v(TAG, "Removing click time tag.");
					Editor editor = mSharedPref.edit();
					editor.remove(WatchActivity.PREF_AD_CLICK_TIME).commit();
				}
			}
		}
		mAutoHideAD.setSummary(getString(R.string.pref_autohide_ad_sum));
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {

		return false;
	}

	private void setupAdLayout() {
		/* 下面两行只用于测试,完成后一定要去掉,参考文挡说明 */
		// AdViewTargeting.setUpdateMode(UpdateMode.EVERYTIME); //
		// 保证每次都从服务器取配置
		AdViewTargeting.setRunMode(RunMode.NORMAL); // 保证所有选中的广告公司都为测试状态
		/* 下面这句方便开发者进行发布渠道统计,详细调用可以参考java doc */
		// AdViewTargeting.setChannel(Channel.GOOGLEMARKET);
		AdViewLayout adViewLayout = new AdViewLayout(this,
				"SDK20122309480217x9sp4og4fxrj2ur");

		ViewGroup adLayout = (ViewGroup) findViewById(R.id.adPrefLayout);
		adLayout.addView(adViewLayout);
		adLayout.invalidate();
	}
}