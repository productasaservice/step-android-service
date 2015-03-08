package com.discover.step;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.discover.step.bl.PrefManager;
import com.discover.step.ui.MainActivity;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SaveCallback;

/**
 * Created by Morpheus on 2014.12.28..
 */
public class StepApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();

        //init parse core data connection.
        Parse.initialize(this, "sKZmGMYBHLvpFCDtMwnYuMdtw0kbx9V0EbfbwEN9", "dFy6L1C8mtxWNRucfcGqdp8XBcl6UH8baAbFZLCy");

        // Specify an Activity to handle all pushes by default.
        //PushService.setDefaultPushCallback(this, MainActivity.class);
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });

        //set default value to highlighted mode.
        PrefManager.getInstance().setIsHighlightedEnabled(false);

        initImageLoader(mContext);
    }

    public static Context getContext() {
        return mContext;
    }

    /**
     * Initialize image loader
     * @param context
     */
    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).build();

        ImageLoader.getInstance().init(config);
    }
}
