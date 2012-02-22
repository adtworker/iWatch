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

public class Settings extends PreferenceActivity
		implements
			Preference.OnPreferenceChangeListener {

	final static String TAG = "Settings";
	SharedPreferences mSharedPref;

	// private CheckBoxPreference mOrientation;
	private CheckBoxPreference mAutoHideClock;
	private CheckBoxPreference mAutoHideAD;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		mSharedPref = getSharedPreferences(WatchActivity.PREFERENCES,
				Context.MODE_PRIVATE);
		mAutoHideClock = (CheckBoxPreference) findPreference(WatchActivity.PREF_AUTOHIDE_CLOCK);
		mAutoHideAD = (CheckBoxPreference) findPreference(WatchActivity.PREF_AUTOHIDE_AD);
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

		return super.onPreferenceTreeClick(prefScreen, preference);
	}

	protected void refresh() {
		mAutoHideClock.setChecked(mSharedPref.getBoolean(
				WatchActivity.PREF_AUTOHIDE_CLOCK, true));

		mAutoHideAD.setChecked(mSharedPref.getBoolean(
				WatchActivity.PREF_AUTOHIDE_AD, false));

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
}