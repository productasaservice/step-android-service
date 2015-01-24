package com.discover.step.ui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.discover.step.R;
import com.discover.step.bc.ServerConnector;
import com.discover.step.bl.UserManager;
import com.discover.step.model.User;
import com.discover.step.social.FbHandlerV3;
import com.discover.step.social.FbHandlerV3Listener;
import com.discover.step.social.GooglePlusHandler;
import com.discover.step.social.GooglePlusListener;

public class LoginActivity extends SocialActivity {

    private Button mFacebookLoginBt, mGooglePlusLoginBt;
    private LinearLayout mProgressLl;

    public static boolean isFacebookBtnClicked;
    public static boolean isFacebookConnectionAttempt;
    public static boolean isGooglePlusBtnClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        isFacebookBtnClicked = false;
        isFacebookConnectionAttempt = false;
        isGooglePlusBtnClicked = false;

        mFacebookLoginBt = (Button) findViewById(R.id.login_facebookBt);
        mGooglePlusLoginBt = (Button) findViewById(R.id.login_googlePlusBt);
        mProgressLl = (LinearLayout) findViewById(R.id.login_progressLl);

        mFacebookLoginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    isFacebookConnectionAttempt = true;
                    mProgressLl.setVisibility(View.VISIBLE);
                    getFacebook().login();
                    mFacebookLoginBt.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mGooglePlusLoginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getGooglePlus().login();
            }
        });

        setOnFacebookConnectedListener(new FbHandlerV3Listener() {
            @Override
            public void onConnected(FbHandlerV3 facebook) {
                if (!isFacebookBtnClicked) {
                    User current = facebook.getUser();
                    UserManager.getInstance().doLogin(current);
                    mFacebookLoginBt.setEnabled(true);
                    isFacebookBtnClicked = true;

                    ServerConnector.getInstance().sendUserData(current);

                    startMainActivity();
                }
            }

            @Override
            public void onDisconnected(){
                if (isFacebookConnectionAttempt) {
                    mProgressLl.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Login fail...", Toast.LENGTH_SHORT).show();
                    isFacebookConnectionAttempt = false;
                    mFacebookLoginBt.setEnabled(true);
                }
            }
        });

        setOnGooglePlusConnectedListener(new GooglePlusListener() {
            @Override
            public void onConnected(GooglePlusHandler googleplus) {
                User current = googleplus.getUser();
                UserManager.getInstance().doLogin(current);

                startMainActivity();
            }

            @Override
            public void onDisconnected() {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    /**
     * Start main activity
     */
    private void startMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));

        finish();
    }
}
