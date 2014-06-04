/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PeekSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "PeekSettings";
    private static final String KEY_PEEK = "notification_peek";
    private static final String KEY_PEEK_PICKUP_TIMEOUT = "peek_pickup_timeout";
    private static final String KEY_PEEK_SCREEN_TIMEOUT = "peek_screen_timeout";

    private CheckBoxPreference mNotificationPeek;
    private ListPreference mPeekPickupTimeout;
    private ListPreference mPeekScreenTimeout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateSettings();
        
        updatePeekCheckbox();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mNotificationPeek) {
            Settings.System.putInt(getContentResolver(), Settings.System.PEEK_STATE,
                    mNotificationPeek.isChecked() ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object value) {
        if (pref == mPeekPickupTimeout) {
            int peekTimeout = Integer.valueOf((String) value);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.PEEK_PICKUP_TIMEOUT,
                    peekTimeout, UserHandle.USER_CURRENT);
            updatePeekTimeoutOptions(value);
            return true;
        } else if (pref == mPeekScreenTimeout) {
            int peekScreenTimeout = Integer.valueOf((String) value);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.PEEK_SCREEN_TIMEOUT,
                    peekScreenTimeout, UserHandle.USER_CURRENT);
            updatePeekScreenTimeoutOptions(value);
            return true;
        }
        return false;
    }

    private void updateSettings() {
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.peek_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        mNotificationPeek = (CheckBoxPreference) findPreference(KEY_PEEK);
        mNotificationPeek.setPersistent(false);

        mPeekPickupTimeout = (ListPreference) prefSet.findPreference(KEY_PEEK_PICKUP_TIMEOUT);
        int peekTimeout = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.PEEK_PICKUP_TIMEOUT, 0, UserHandle.USER_CURRENT);
        mPeekPickupTimeout.setValue(String.valueOf(peekTimeout));
        mPeekPickupTimeout.setSummary(mPeekPickupTimeout.getEntry());
        mPeekPickupTimeout.setOnPreferenceChangeListener(this);

        mPeekScreenTimeout = (ListPreference) prefSet.findPreference(KEY_PEEK_SCREEN_TIMEOUT);
        int peekScreenTimeout = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.PEEK_SCREEN_TIMEOUT, 0, UserHandle.USER_CURRENT);
        mPeekScreenTimeout.setValue(String.valueOf(peekScreenTimeout));
        mPeekScreenTimeout.setSummary(mPeekScreenTimeout.getEntry());
        mPeekScreenTimeout.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updatePeekTimeoutOptions(Object newValue) {
        int index = mPeekPickupTimeout.findIndexOfValue((String) newValue);
        int value = Integer.valueOf((String) newValue);
        Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.System.PEEK_PICKUP_TIMEOUT, value);
        mPeekPickupTimeout.setSummary(mPeekPickupTimeout.getEntries()[index]);
    }

    private void updatePeekScreenTimeoutOptions(Object newValue) {
        int index = mPeekScreenTimeout.findIndexOfValue((String) newValue);
        int value = Integer.valueOf((String) newValue);
        Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.System.PEEK_SCREEN_TIMEOUT, value);
        mPeekScreenTimeout.setSummary(mPeekScreenTimeout.getEntries()[index]);
    }

    private void updatePeekCheckbox() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.PEEK_STATE, 0) == 1;
        mNotificationPeek.setChecked(enabled);
    }
}