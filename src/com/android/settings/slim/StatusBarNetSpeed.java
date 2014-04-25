/*
 * Copyright (C) 2013 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.slim;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.util.slim.DeviceUtils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarNetSpeed extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBarNetSpeed";

    private static final String PREF_NETWORK_SPEED_COLOR = "network_speed_color";

    private ColorPickerPreference mStatusBarTrafficColor;

    private static final String STATUS_BAR_TRAFFIC_ENABLE = "status_bar_traffic_enable";
    private static final String STATUS_BAR_TRAFFIC_HIDE = "status_bar_traffic_hide";
    private static final String STATUS_BAR_TRAFFIC_SUMMARY = "status_bar_traffic_summary";
    private static final String STATUS_BAR_NETWORK_STATS = "status_bar_show_network_stats";
    private static final String STATUS_BAR_NETWORK_STATS_UPDATE = "status_bar_network_status_update";


    private CheckBoxPreference mStatusBarTraffic_enable;
    private CheckBoxPreference mStatusBarTraffic_hide;
    private ListPreference mStatusBarTraffic_summary;
    private ListPreference mStatusBarNetStatsUpdate;
    private CheckBoxPreference mStatusBarNetworkStats;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.statusbar_net_speed);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarTraffic_enable = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC_ENABLE);
        mStatusBarTraffic_enable.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_TRAFFIC_ENABLE, 0) == 1));

        mStatusBarTraffic_hide = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC_HIDE);
        mStatusBarTraffic_hide.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_TRAFFIC_HIDE, 0) == 1));

        mStatusBarTraffic_summary = (ListPreference) findPreference(STATUS_BAR_TRAFFIC_SUMMARY);
        mStatusBarTraffic_summary.setOnPreferenceChangeListener(this);
        mStatusBarTraffic_summary.setValue((Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_TRAFFIC_SUMMARY, 3000)) + "");


        mStatusBarNetworkStats = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS);
        mStatusBarNetStatsUpdate = (ListPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS_UPDATE);
        mStatusBarNetworkStats.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS, 0) == 1));

        long statsUpdate = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, 500);
        mStatusBarNetStatsUpdate.setValue(String.valueOf(statsUpdate));
        mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntry());
        mStatusBarNetStatsUpdate.setOnPreferenceChangeListener(this);

        mStatusBarTraffic_summary.setEnabled(!mStatusBarNetworkStats.isChecked());

        refreshSettings();

    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarTraffic_summary) {
            int val = Integer.valueOf((String) objValue);
            int index = mStatusBarTraffic_summary.findIndexOfValue((String) objValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_TRAFFIC_SUMMARY, val);
            mStatusBarTraffic_summary.setSummary(mStatusBarTraffic_summary.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarNetStatsUpdate) {
            long updateInterval = Long.valueOf((String) objValue);
            int index = mStatusBarNetStatsUpdate.findIndexOfValue((String) objValue);
            Settings.System.putLong(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, updateInterval);
            mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarTrafficColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_TRAFFIC_COLOR, intHex);
            return true;
        }
        return false;
    }

    public void refreshSettings() {
        mStatusBarTrafficColor = (ColorPickerPreference) findPreference(PREF_NETWORK_SPEED_COLOR);
        mStatusBarTrafficColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_TRAFFIC_COLOR, 0xff000000);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
        mStatusBarTrafficColor.setNewPreviewColor(intColor);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mStatusBarTraffic_enable) {
            value = mStatusBarTraffic_enable.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_TRAFFIC_ENABLE, value ? 1 : 0);
        } else if (preference == mStatusBarTraffic_hide) {
            value = mStatusBarTraffic_hide.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_TRAFFIC_HIDE, value ? 1 : 0);
        } else if (preference == mStatusBarNetworkStats) {
            value = mStatusBarNetworkStats.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS, value ? 1 : 0);
                    mStatusBarTraffic_summary.setEnabled(!value);
        }
        return true;
    }
}
