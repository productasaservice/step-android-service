package com.discover.step.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.discover.step.social.FbHandlerV3;
import com.discover.step.social.FbHandlerV3Listener;
import com.discover.step.social.GooglePlusHandler;
import com.discover.step.social.GooglePlusListener;

/**
 * Created by Geri on 2014.09.08..
 */
public class SocialActivity extends ActionBarActivity {

    private FbHandlerV3 mFacebook;
    private GooglePlusHandler mGooglePlus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFacebook(savedInstanceState);
        initGooglePlus();
    }

    private void initFacebook(Bundle savedInstanceState) {
        mFacebook = FbHandlerV3.getInstance(SocialActivity.this);
        mFacebook.setActivity(this);
        mFacebook.create(savedInstanceState);
        mFacebook.addPermissions(new String[]{"public_profile","email","user_friends"});
    }

    private void initGooglePlus() {
        mGooglePlus = GooglePlusHandler.newInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGooglePlus.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFacebook.resume();
        Log.d("test--","meghívva");
        mFacebook.checkForDeepLinking();
    }

    @Override
    public void onPause() {
        super.onPause();
        mFacebook.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (!mGooglePlus.isConnecting()) {
//            mGooglePlus.disconnect();
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFacebook.destroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mFacebook.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        if (requestCode == GooglePlusHandler.RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mGooglePlus.signInClicked = false;
            }

            mGooglePlus.intentInProgress = false;

            Log.d("test--","onAResult, plus" + mGooglePlus.isConnecting());
            if (!mGooglePlus.isConnecting()) {
                mGooglePlus.connect();
            }

        } else {
            mFacebook.onActivityResult(requestCode, responseCode, intent);
        }
    }

    public void setOnFacebookConnectedListener(FbHandlerV3Listener listener) {
        mFacebook.addListener(listener);
    }

    public void setOnGooglePlusConnectedListener(GooglePlusListener listener) {
        mGooglePlus.addConnectionListener(listener);
    }

    public FbHandlerV3 getFacebook() {
        return mFacebook;
    }

    public GooglePlusHandler getGooglePlus() {
        return mGooglePlus;
    }

    public void clearSocialHandlers() {
        mFacebook.logout();
        mGooglePlus.logout();
    }
}
