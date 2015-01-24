package com.discover.step.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.discover.step.model.User;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;

import java.text.ParseException;

/**
 * Created by Geri on 2014.06.19..
 */
public class FbHandlerV3 {
    private static FbHandlerV3 mInstane;
    private static FbHandlerV3Listener mListeners;
    private static UiLifecycleHelper mUiHelper;
    private static GraphUser mCurrentUser = null;
    private static String[] mPermissions = null;

    private static Activity mActivity;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private static void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            final Session f = session;
            // make request to the /me API
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                // callback after Graph API response with user object
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        mCurrentUser = user;

                        if(mListeners != null) {
                           mListeners.onConnected(mInstane);
                        }
                    }
                }

            }).executeAsync();

        } else if (state.isClosed()) {
            if(mListeners != null) {
                mListeners.onDisconnected();
            }
        }
    }

    private FbHandlerV3() {
        mListeners = null;
        mUiHelper = new UiLifecycleHelper(mActivity, callback);
    }

    public static FbHandlerV3 newInstance(Activity activity) {
        mActivity = activity;
        if(mInstane == null) {
            mInstane = new FbHandlerV3();
        }
        return mInstane;
    }

    public static FbHandlerV3 getInstance(Activity activity) {
        mActivity = activity;
        if(mInstane == null) {
            mInstane = new FbHandlerV3();
        }
        return mInstane;
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public static void create(Bundle savedInstanceState) {
        mUiHelper.onCreate(savedInstanceState);
    }

    public static void resume() {
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }
        mUiHelper.onResume();
    }

    public static void pause() {
        mUiHelper.onResume();
    }

    public static void destroy() {
        mUiHelper.onDestroy();
    }

    public static void onSaveInstanceState(Bundle outState) {
        mUiHelper.onSaveInstanceState(outState);
    }

    public static void onActivityResult(int requestCode, int responseCode, Intent data) {
        mUiHelper.onActivityResult(requestCode, responseCode, data);
    }

    public void addListener(FbHandlerV3Listener listener) {
        mListeners = listener;
    }

    public void removeListener(FbHandlerV3Listener listener) {
        mListeners = null;
    }

    public void addPermissions(String[] permissions) {
        mPermissions = permissions;
    }

    public boolean isSessionValid() {
        return Session.getActiveSession().isOpened() && Session.getActiveSession().getAccessToken() != null ;
    }

    public void login() throws Exception {
        if (mPermissions == null) {
            throw new Exception("Missing Permissions");
        }

        Session session = Session.getActiveSession();
        if (session == null || (!session.isOpened() && !session.isClosed())) {
            session.openForRead(new Session.OpenRequest(mActivity).setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK)
                    .setPermissions(mPermissions));
        } else {
            Session.openActiveSession(mActivity, true, callback);
        }
    }

    public void logout() {
        if (Session.getActiveSession() != null) {
            Session.getActiveSession().closeAndClearTokenInformation();
        } else {
            Session.setActiveSession(new Session(mActivity));
            Session.getActiveSession().closeAndClearTokenInformation();
        }

        //Session.setActiveSession(null);
    }

    public GraphUser getCurrentUser() {
        return mCurrentUser;
    }

    public String getAccessToken() {
        String token = Session.getActiveSession().getAccessToken();
        return token;
    }

    public User getUser() {
        User user = new User();
        user.social_id = mCurrentUser.getId();
        user.first_name = mCurrentUser.getFirstName();
        user.last_name = mCurrentUser.getLastName();

        if (mCurrentUser.asMap().get("email").toString() != null) {
            user.email = mCurrentUser.asMap().get("email").toString();
        }

        user.picture_url = "http://graph.facebook.com/" + mCurrentUser.getId() + "/picture?height=332&width=332";
        user.login_type = User.FACEBOOK;

        return user;
    }

    public void publishStory(final String name, final String caption, final String description, final String link, final String picture_url) {
        Session session = Session.getActiveSession();
        if (!session.isOpened()) {
            Session.openActiveSession(mActivity,true, new Session.StatusCallback(){
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    if(session.isOpened()) {
                        publishFeedDialog(name,caption,description,link,picture_url);
                    }
                }
            });
        } else {
            publishFeedDialog(name,caption,description,link,picture_url);
        }
    }

    private void publishFeedDialog(String name, String caption, String description, String link, String picture_url) {
        Bundle params = new Bundle();
        params.putString("name", name);
        params.putString("caption", caption);
        params.putString("description", description);
        params.putString("link", link);
        params.putString("picture", picture_url);

        WebDialog feedDialog = (
                new WebDialog.FeedDialogBuilder(mActivity,
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                Toast.makeText(mActivity,
                                        "Story was posted",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(mActivity.getApplicationContext(),
                                        "Publish cancelled",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(mActivity.getApplicationContext(),
                                    "Error posting story",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                })
                .build();
        feedDialog.show();
    }

    public interface OnUserDataLoaded{
        public void onComplete(User user);
    }

}
