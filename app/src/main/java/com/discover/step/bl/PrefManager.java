package com.discover.step.bl;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.discover.step.StepApplication;

/**
 * Created by Geri on 2015.01.25..
 */
public class PrefManager {

    private static PrefManager mInstance = null;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private static final String IS_INFORMATION_SCREEN_WAS_SHOWN = "IS_INFORMATION_SCREEN_WAS_SHOWN";
    private static final String IS_INITIAL_DATA_SYNCED = "IS_INITIAL_DATA_SYNCED";
    private static final String IS_HIGHLIGHTED_ENABLED = "IS_HIGHLIGHTED_ENABLED";

    public static PrefManager getInstance() {
        if (mInstance == null) {
            mInstance = new PrefManager();
        }
        return mInstance;
    }

    private PrefManager() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(StepApplication.getContext());
        mEditor = mSharedPreferences.edit();
    }


    public void setUserStepCount(String user_social_id, int step_count) {
        mEditor.putInt(user_social_id,step_count);
        mEditor.commit();
    }

    public int getUserStepCount(String user_social_id) {
        return mSharedPreferences.getInt(user_social_id,0);
    }

    public void setIsInformationScreenWasShown() {
        mEditor.putBoolean(IS_INFORMATION_SCREEN_WAS_SHOWN,true);
        mEditor.commit();
    }

    public boolean isInformationScreenWasShown() {
        return mSharedPreferences.getBoolean(IS_INFORMATION_SCREEN_WAS_SHOWN,false);
    }

    public void setIsInitialDataSynced() {
        mEditor.putBoolean(IS_INITIAL_DATA_SYNCED,true);
        mEditor.commit();
    }

    public boolean isInitialDataSynced() {
        return mSharedPreferences.getBoolean(IS_INITIAL_DATA_SYNCED,false);
    }

    public void setIsHighlightedEnabled(boolean isHighlightedEnabled) {
        mEditor.putBoolean(IS_HIGHLIGHTED_ENABLED,isHighlightedEnabled);
        mEditor.commit();
    }

    public boolean getIsHighlightedEnabled() {
        return mSharedPreferences.getBoolean(IS_HIGHLIGHTED_ENABLED,false);
    }
}
