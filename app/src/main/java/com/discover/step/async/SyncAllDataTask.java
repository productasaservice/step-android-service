package com.discover.step.async;

import android.location.Location;
import android.util.Log;

import com.discover.step.Session;
import com.discover.step.bc.ServerConnector;
import com.discover.step.bl.AchievementManager;
import com.discover.step.bl.ChallengeManager;
import com.discover.step.bl.GPSHandlerManager;
import com.discover.step.bl.StepManager;
import com.discover.step.bl.UserManager;
import com.discover.step.model.User;

/**
 * Created by Geri on 2015.01.29..
 */
public class SyncAllDataTask extends SafeAsyncTask<Void,Void,Void> {

    private OnSyncReadyListener mListener;


    public SyncAllDataTask(OnSyncReadyListener listener) {
        mListener = listener;
    }

    @Override
    protected Void doWorkInBackground(Void... params) throws Exception {
        AchievementManager.getInstance().downloadBadges();
        AchievementManager.getInstance().downloadAchievements();
        StepManager.getInstance().downloadDayList();
        ChallengeManager.getInstance().downloadChallengeByUserId(Session.getAuthenticatedUserSocialId());

        return null;
    }

    @Override
    protected void onSuccess(Void aVoid) {
        super.onSuccess(aVoid);
        mListener.onReady();
    }

    public interface OnSyncReadyListener {
        public void onReady();
    }

}
