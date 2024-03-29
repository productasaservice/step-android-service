package com.discover.step.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.discover.step.R;
import com.discover.step.async.SafeAsyncTask;
import com.discover.step.async.SyncAllDataTask;
import com.discover.step.bc.DatabaseConnector;
import com.discover.step.bl.PrefManager;
import com.discover.step.social.FbHandlerV3;
import com.discover.step.helper.StepHelper;

public class SplashActivity extends SocialActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        StepHelper.getSHA1Key();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new CheckUserDataInLocalDbTask().execute();
            }
        },1000);
    }

    private class CheckUserDataInLocalDbTask extends SafeAsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doWorkInBackground(Void... params) throws Exception {
            return DatabaseConnector.getInstance().getLoggedInUser() != null;
        }

        @Override
        protected void onSuccess(Boolean success) {
            super.onSuccess(success);
            startMainActivity(success);
        }
    }

    /**
     * Start main activity
     */
    private void startMainActivity(boolean isLoggedIn) {
        if (isLoggedIn) {
            //Download initial data.
            if (!PrefManager.getInstance().isInitialDataSynced()) {
                new SyncAllDataTask(new SyncAllDataTask.OnSyncReadyListener() {
                    @Override
                    public void onReady() {
                        PrefManager.getInstance().setIsInitialDataSynced();
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                }).execute();
            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }

        } else {
            //Go to login screen.
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }

        finish();
    }

}
