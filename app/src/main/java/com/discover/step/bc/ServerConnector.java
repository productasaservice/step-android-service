package com.discover.step.bc;

import android.util.Log;

import com.discover.step.model.StepPoint;
import com.discover.step.model.User;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geri on 2015.01.24..
 */
public class ServerConnector {
    private static ServerConnector mInstance = null;

    public static ServerConnector getInstance() {
        if (mInstance == null) {
            mInstance = new ServerConnector();
        }
        return mInstance;
    }

    public void sendStepPoints(List<StepPoint> stepPointList) {
        List<ParseObject> requestList = new ArrayList<>();
        for (StepPoint sp : stepPointList) {
            requestList.add(sp.toParseObject());
        }
        ParseObject.saveAllInBackground(requestList);
    }

    public void sendUserData(final User user) {
        getUserDataBy(user.social_id, new OnUserRetrieveListener() {
            @Override
            public void onReady(User response, boolean isSuccess) {
                if (!isSuccess) {
                    user.toParseObject().saveInBackground();
                }
            }
        });
    }

    public void getUserDataBy(String social_id, final OnUserRetrieveListener listener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("StepUser");
        query.whereEqualTo("social_id", social_id);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (listener == null) return;

                if (scoreList != null && !scoreList.isEmpty()) {
                    User u = new User();
                    listener.onReady(u.toUser(scoreList.get(0)),true);
                } else {
                    listener.onReady(null,false);
                }
            }
        });
    }

    public interface OnUserRetrieveListener {
        public void onReady(User user, boolean isSuccess);
    }
}
