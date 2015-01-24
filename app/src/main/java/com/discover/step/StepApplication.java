package com.discover.step;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;

/**
 * Created by Morpheus on 2014.12.28..
 */
public class StepApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();

        Parse.initialize(this, "sKZmGMYBHLvpFCDtMwnYuMdtw0kbx9V0EbfbwEN9", "dFy6L1C8mtxWNRucfcGqdp8XBcl6UH8baAbFZLCy");
    }

    public static Context getContext() {
        return mContext;
    }
}
