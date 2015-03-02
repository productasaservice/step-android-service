package com.discover.step.social;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.discover.step.async.SafeAsyncTask;
import com.discover.step.model.User;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

import org.apache.http.HttpRequestFactory;

import java.io.IOException;

/**
 * Created by Geri on 2014.08.27..
 */

public class GooglePlusHandler implements ResultCallback<People.LoadPeopleResult>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    /*Client used to interact with Google APIs*/
    private  GoogleApiClient mGoogleApiClient;
    private  ConnectionResult mConnectionResult;
    private  Person mCurrentPerson;

    private static Activity mActivity;
    private static GooglePlusListener mListeners;

  /* A flag indicating that a PendingIntent is in progress and prevents
   * us from starting further intents.
   */
    public static boolean intentInProgress;
    public static boolean signInClicked;

    /* Request code used to invoke sign in user interactions. */
    public static final int RC_SIGN_IN = 600613;

    private static GooglePlusHandler mInstance = null;

    public static GooglePlusHandler newInstance(Activity activity) {
        mActivity = activity;
        if(mInstance == null) {
            mInstance = new GooglePlusHandler();
        }

        return mInstance;
    }

    public static GooglePlusHandler getInstance() {
        if(mInstance == null) {
            mInstance = new GooglePlusHandler();
        }

        return mInstance;
    }

    private GooglePlusHandler() {
        initGoogleClient();
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    private void initGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    public void login() {
        signInClicked = true;
        if(!mGoogleApiClient.isConnecting()) {
            resolveSignInError();
        }
    }

    public void logout() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();

            if(mListeners != null) {
               mListeners.onDisconnected();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        signInClicked = false;

        Log.d("test--","connected");

//        //new vmiTask().execute();
//
//
//
//        Intent res = new Intent();
//        res.addCategory("account:kole.geri@gmail.com");
//        res.addCategory("scope:oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile");
//        res.putExtra("service", "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile");
//        Bundle extra= new Bundle();
//        extra.putString("androidPackageName","com.discover.step");
//        res.putExtra("callerExtras",extra);
//        res.putExtra("androidPackageName","com.discover.step");
//        res.putExtra("authAccount","kole.geri@gmail.com");
//
//        String mPackage = "com.google.android.gms";
//        String mClass = "com.google.android.gms.auth.TokenActivity";
//        res.setComponent(new ComponentName(mPackage,mClass));
//        mActivity.startActivityForResult(res,100);

       if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            mCurrentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
       }

        if(mListeners != null && mCurrentPerson != null) {
            mListeners.onConnected(mInstance);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!intentInProgress) {
            // Store the ConnectionResult so that we can use it later when the user clicks
            // 'sign-in'.
            mConnectionResult = result;

            if (signInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                intentInProgress = true;
                mActivity.startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                intentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    public Person getCurrentPerson() {
        return mCurrentPerson;
    }

    public String getCurrentPersonEmail() {
        return Plus.AccountApi.getAccountName(mGoogleApiClient);
    }

    public void addConnectionListener(GooglePlusListener listener) {
            mListeners = listener;
    }

    public void removeConnectionListener(GooglePlusListener listener) {
        mListeners = null;
    }

    public User getUser() {
        User user = new User();
        user.social_id = mCurrentPerson.getId();
        user.first_name = mCurrentPerson.getName().getGivenName();
        user.last_name = mCurrentPerson.getName().getFamilyName();
        user.email = getCurrentPersonEmail();
        user.picture_url = mCurrentPerson.getImage().getUrl();
        return user;
    }

    public void connect(){
        mGoogleApiClient.connect();
    }

    public boolean isConnecting() {
        return mGoogleApiClient.isConnecting();
    }

    public boolean isConnected() { return mGoogleApiClient.isConnected();}

    public void disconnect() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResult(People.LoadPeopleResult loadPeopleResult) {
        Log.d("test--","result: " + loadPeopleResult.getStatus());
        if (loadPeopleResult.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = loadPeopleResult.getPersonBuffer();

            Log.d("test--","onResultban vagyok...");
        }
    }

    private class vmiTask extends SafeAsyncTask<Void,Void,Void> {

        @Override
        protected Void doWorkInBackground(Void... params) throws Exception {
//            String scope="oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
//            try {
//                final String token = GoogleAuthUtil.getToken(mActivity, "kole.geri@gmail.com", scope);
//                Log.d("test--","token: " + token);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (GoogleAuthException e) {
//                e.printStackTrace();
//            }

            return null;
        }
    }
}
