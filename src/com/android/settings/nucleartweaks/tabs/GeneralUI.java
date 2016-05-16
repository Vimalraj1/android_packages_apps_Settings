package com.android.settings.nucleartweaks.tabs;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.Utils;

public class GeneralUI extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "GeneralUI";

    private static final String KEY_LCD_DENSITY = "lcd_density";

    private ListPreference mLcdDensityPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.generalui);
        PreferenceScreen prefSet = getPreferenceScreen();

        ContentResolver resolver = getActivity().getContentResolver();

        mLcdDensityPreference = (ListPreference) findPreference(KEY_LCD_DENSITY);
        if (mLcdDensityPreference != null) {
            if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
                interfacePrefs.removePreference(mLcdDensityPreference);
            } else {
                int defaultDensity = getDefaultDensity();
                int currentDensity = getCurrentDensity();
                if (currentDensity < 10 || currentDensity >= 1000) {
                    // Unsupported value, force default
                    currentDensity = defaultDensity;
                }

                int factor = defaultDensity >= 480 ? 40 : 20;
                int minimumDensity = defaultDensity - 4 * factor;
                int currentIndex = -1;
                String[] densityEntries = new String[7];
                String[] densityValues = new String[7];
                for (int idx = 0; idx < 7; ++idx) {
                    int val = minimumDensity + factor * idx;
                    int valueFormatResId = val == defaultDensity
                            ? R.string.lcd_density_default_value_format
                            : R.string.lcd_density_value_format;

                    densityEntries[idx] = getString(valueFormatResId, val);
                    densityValues[idx] = Integer.toString(val);
                    if (currentDensity == val) {
                        currentIndex = idx;
                    }
                }
                mLcdDensityPreference.setEntries(densityEntries);
                mLcdDensityPreference.setEntryValues(densityValues);
                if (currentIndex != -1) {
                    mLcdDensityPreference.setValueIndex(currentIndex);
                }
                mLcdDensityPreference.setOnPreferenceChangeListener(this);
                updateLcdDensityPreferenceDescription(currentDensity);
            }
        }

      
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.NUCLEARTWEAKS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_LCD_DENSITY.equals(key)) {
            String newValue = (String) objValue;
            String oldValue = mLcdDensityPreference.getValue();
            if (!TextUtils.equals(newValue, oldValue)) {
                showLcdConfirmationDialog((String) objValue);
            }
            return false;
        }
        return true;
    }

    private void updateLcdDensityPreferenceDescription(int currentDensity) {
        final int summaryResId = currentDensity == getDefaultDensity()
                ? R.string.lcd_density_default_value_format : R.string.lcd_density_value_format;
        mLcdDensityPreference.setSummary(getString(summaryResId, currentDensity));
    }

    private void showLcdConfirmationDialog(final String lcdDensity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.lcd_density);
        builder.setMessage(R.string.lcd_density_prompt_message);
        builder.setPositiveButton(R.string.print_restart,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    int value = Integer.parseInt(lcdDensity);
                    writeLcdDensityPreference(getActivity(), value);
                    updateLcdDensityPreferenceDescription(value);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "could not persist display density setting", e);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }
}