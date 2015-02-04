package com.discover.step.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.discover.step.R;
import com.discover.step.async.SyncAllDataTask;
import com.discover.step.bc.ServerConnector;
import com.discover.step.bl.PrefManager;
import com.discover.step.bl.UserManager;
import com.discover.step.model.User;
import com.discover.step.social.FbHandlerV3;
import com.discover.step.social.FbHandlerV3Listener;
import com.discover.step.social.GooglePlusHandler;
import com.discover.step.social.GooglePlusListener;

public class LoginActivity extends SocialActivity {

    private Button mFacebookLoginBt, mGooglePlusLoginBt;
    private LinearLayout mProgressLl;
    private ImageView mLogoIv;

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
        mLogoIv = (ImageView) findViewById(R.id.login_logoIv);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openAnimation();
            }
        },500);

        //StepHelper.getSHA1Key();

        mFacebookLoginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    closeAnimation();
                    isFacebookConnectionAttempt = true;
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

                    //Send user data if it is not inserted into central db.
                    ServerConnector.getInstance().sendUserData(current);

                    //Sync User Data.
                    final User currentUser = UserManager.getInstance().getAuthenticatedUser();
                    ServerConnector.getInstance().getUserDataBy(currentUser.social_id,new ServerConnector.OnServerResponseListener<User>() {
                        @Override
                        public void onReady(User user, boolean isSuccess) {
                            if (isSuccess) {
                                currentUser.steps_count = user.steps_count;
                                UserManager.getInstance().updateUser(currentUser);
                            }

                            syncData();
                        }
                    });
                }
            }

            @Override
            public void onDisconnected(){
                if (isFacebookConnectionAttempt) {
                    openAnimation();
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

    /**
     * Start main activity
     */
    private void syncData() {
        //Download initial data.
        if (!PrefManager.getInstance().isInitialDataSynced()) {
            new SyncAllDataTask(new SyncAllDataTask.OnSyncReadyListener() {
                @Override
                public void onReady() {
                    PrefManager.getInstance().setIsInitialDataSynced();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }).execute();
        } else {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void openAnimation() {
        mProgressLl.setVisibility(View.INVISIBLE);
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_and_hide);
        anim.setInterpolator(new OvershootInterpolator());
        anim.setFillAfter(true);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(300);
        fadeIn.setStartOffset(100);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mFacebookLoginBt.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mFacebookLoginBt.startAnimation(fadeIn);
        mLogoIv.startAnimation(anim);
    }

    private void closeAnimation() {
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        anim.setInterpolator(new OvershootInterpolator());
        anim.setFillAfter(true);
        anim.setDuration(400);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                fadeIn.setDuration(300);
                mProgressLl.startAnimation(fadeIn);
                mProgressLl.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mLogoIv.startAnimation(anim);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //add this
        fadeOut.setDuration(100);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFacebookLoginBt.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mFacebookLoginBt.startAnimation(fadeOut);
        mLogoIv.startAnimation(anim);
    }
}
