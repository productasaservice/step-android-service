package com.discover.step.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.discover.step.R;
import com.discover.step.model.User;
import com.discover.step.ui.MainActivity;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Geri on 2014.06.19..
 */
public class FbHandlerV3 {
    private static FbHandlerV3 mInstane;
    private static FbHandlerV3Listener mListeners;
    private static UiLifecycleHelper mUiHelper;
    private static GraphUser mCurrentUser = null;
    private static String[] mPermissions = null;
    private static boolean isFriendListRequested = false;

    private static Activity mActivity;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
            if (state.isOpened() && isFriendListRequested) {
                isFriendListRequested = false;
                sendChallenge();
            }
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


        if (mCurrentUser.asMap().get("email") != null) {
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

    public void getRequestData(final String inRequestId) {
        // Create a new request for an HTTP GET with the
        // request ID as the Graph path.
        Request request = new Request(Session.getActiveSession(),
                inRequestId, null, HttpMethod.GET, new Request.Callback() {

            @Override
            public void onCompleted(Response response) {
                // Process the returned response
                GraphObject graphObject = response.getGraphObject();
                FacebookRequestError error = response.getError();
                // Default message
                String message = "Incoming request";
                if (graphObject != null) {
                    // Check if there is extra data
                    if (graphObject.getProperty("data") != null) {
                        try {
                            // Get the data, parse info to get the key/value info
                            JSONObject dataObject =
                                    new JSONObject((String)graphObject.getProperty("data"));
                            // Get the value for the key - badge_of_awesomeness
                            String badge =
                                    dataObject.getString("badge_of_awesomeness");
                            // Get the value for the key - social_karma
                            String karma =
                                    dataObject.getString("social_karma");
                            // Get the sender's name
                            JSONObject fromObject =
                                    (JSONObject) graphObject.getProperty("from");
                            String sender = fromObject.getString("name");
                            String title = sender+" sent you a gift";
                            // Create the text for the alert based on the sender
                            // and the data
                            message = title + "\n\n" +
                                    "Badge: " + badge +
                                    " Karma: " + karma;
                        } catch (JSONException e) {
                            message = "Error getting request info";
                        }
                    } else if (error != null) {
                        message = "Error getting request info";
                    }
                }
                Toast.makeText(mActivity.getApplicationContext(),
                        message,
                        Toast.LENGTH_LONG).show();
            }
        });
        // Execute the request asynchronously.
        Request.executeBatchAsync(request);
    }

    private void deleteRequest(String inRequestId) {
        // Create a new request for an HTTP delete with the
        // request ID as the Graph path.
        Request request = new Request(Session.getActiveSession(),
                inRequestId, null, HttpMethod.DELETE, new Request.Callback() {

            @Override
            public void onCompleted(Response response) {
                // Show a confirmation of the deletion
                // when the API call completes successfully.
                Toast.makeText(mActivity.getApplicationContext(), "Request deleted",
                        Toast.LENGTH_SHORT).show();
            }
        });
        // Execute the request asynchronously.
        Request.executeBatchAsync(request);
    }

    public void sendChallenge() {
        Bundle params = new Bundle();
        params.putString("message", "I just smashed " + 0 +
                " friends! Can you beat it?");
        params.putString("data",
                "{\"badge_of_awesomeness\":\"1\"," +
                        "\"social_karma\":\"5\"}");

        showDialogWithoutNotificationBar("apprequests", params);
    }

    private void showDialogWithoutNotificationBar(String dialogAction, Bundle dialogParams) {
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())) {

        WebDialog dialog = new WebDialog.Builder(mActivity, Session.getActiveSession(), dialogAction, dialogParams).
                setOnCompleteListener(new WebDialog.OnCompleteListener() {
                    @Override
                    public void onComplete(Bundle values, FacebookException error) {
                        if (error != null && !(error instanceof FacebookOperationCanceledException)) {
//                            ((HomeActivity)getActivity()).
//                                    showError(getResources().getString(R.string.network_error), false);
                        }

                    }
                }).build();

        Window dialog_window = dialog.getWindow();
        dialog_window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        dialogAction = action;
//        dialogParams = params;
        dialog.show();
        } else {
            isFriendListRequested = true;
            Session.openActiveSession(mActivity, true, callback);
        }
    }

    public void getFriendsList() {
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())) {

            Request friendRequest = Request.newMyFriendsRequest(session,new Request.GraphUserListCallback() {
                @Override
                public void onCompleted(List<GraphUser> graphUsers, Response response) {
                    Log.d("test--","response: " + graphUsers.size() + " re: " + response);
                }
            });
            friendRequest.executeAsync();



        } else {
            isFriendListRequested = true;
            Session.openActiveSession(mActivity, true, callback);
        }


    }

    public interface OnUserDataLoaded{
        public void onComplete(User user);
    }

    public void checkForDeepLinking() {
        if (mCurrentUser != null) {
            Uri target = mActivity.getIntent().getData();
            if (target != null) {
                Intent i = new Intent(mActivity, MainActivity.class);
                String graphRequestIDsForSendingUser = target.getQueryParameter("request_ids");
                String feedPostIDForSendingUser = target.getQueryParameter("challenge_brag");
                if (graphRequestIDsForSendingUser != null) {
                    String [] graphRequestIDsForSendingUsers = graphRequestIDsForSendingUser.split(",");
                    String graphRequestIDForSendingUser =
                            graphRequestIDsForSendingUsers[graphRequestIDsForSendingUsers.length-1];
                    Bundle bundle = new Bundle();
                    bundle.putString("request_id", graphRequestIDForSendingUser);
                    i.putExtras(bundle);
                    //gameLaunchedFromDeepLinking = true;
                    mActivity.startActivityForResult(i, 0);

                    Request deleteFBRequestRequest = new Request(Session.getActiveSession(),
                            graphRequestIDForSendingUser + "_" + mCurrentUser.getId(),
                            new Bundle(),
                            HttpMethod.DELETE,
                            new Request.Callback() {
                                @Override
                                public void onCompleted(Response response) {
                                    FacebookRequestError error = response.getError();
                                    if (error != null) {
                                        Log.e("test--",
                                                "Deleting consumed Request failed: " + error.getErrorMessage());
                                    } else {
                                        Log.i("test--", "Consumed Request deleted");
                                    }
                                }
                            });
                    Request.executeBatchAsync(deleteFBRequestRequest);
                }
            }
        }
    }

}
