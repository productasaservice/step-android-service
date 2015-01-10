package com.discover.step;

import android.app.Application;
import android.content.Context;

/**
 * Created by Morpheus on 2014.12.28..
 */
public class StepApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
