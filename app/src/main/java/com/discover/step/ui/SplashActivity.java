package com.discover.step.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.discover.step.Config;
import com.discover.step.R;
import com.discover.step.async.SafeAsyncTask;
import com.discover.step.bc.DatabaseConnector;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SplashActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new CheckAuthenticationTask().execute();
    }

    /**
     * Start main activity
     */
    private void startMainActivity(boolean isLoggedIn) {
        if (isLoggedIn) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }

        finish();
    }

    private class CheckAuthenticationTask extends SafeAsyncTask<Void,Void,Boolean> {

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
}
