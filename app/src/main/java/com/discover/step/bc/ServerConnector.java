package com.discover.step.bc;

import android.util.Log;

import com.discover.step.async.SyncAllDataTask;
import com.discover.step.bl.NotificationManager;
import com.discover.step.bl.StepManager;
import com.discover.step.bl.UserManager;
import com.discover.step.model.Achievement;
import com.discover.step.model.Badge;
import com.discover.step.model.Challenge;
import com.discover.step.model.Day;
import com.discover.step.model.StepPoint;
import com.discover.step.model.User;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public void sendStepPoints(final List<StepPoint> stepPointList) {
        List<ParseObject> requestList = new ArrayList<>();
        for (StepPoint sp : stepPointList) {
            requestList.add(sp.toParseObject());
        }
        ParseObject.saveAllInBackground(requestList,new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if (e == null) {
                    for (int i = 0; i < stepPointList.size(); i++) {
                        stepPointList.get(i).isSynced = true;
                    }
                    StepManager.getInstance().updateStepPoints(stepPointList);
                }
            }
        });
    }

    public void sendDays(final List<Day> dayList) {
        List<ParseObject> requestList = new ArrayList<>();
        for (Day day : dayList) {
            requestList.add(day.toParseObject());
        }
        ParseObject.saveAllInBackground(requestList,new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    for (Day day : dayList) {
                        day.isSynced = true;
                        StepManager.getInstance().setOrUpdateDay(day);
                    }
                }
            }
        });
    }

    public void sendUserData(final User user) {
        getUserDataBy(user.social_id, new OnServerResponseListener<User>() {
            @Override
            public void onReady(User response, boolean isSuccess) {
                if (!isSuccess) {
                    //Insert new.
                    user.toParseObject().saveInBackground();
                }
            }
        });
    }

    public void sendBadge(final Badge badge) {
        ParseObject parseObject = badge.toParseObject();
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.d("ServerConnector","send badge error: " + e.getMessage());
                }
            }
        });
    }

    public void sendDay(final Day day) {
        ParseObject parseObject = day.toParseObject();
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    day.isSynced = true;
                    StepManager.getInstance().setOrUpdateDay(day);
                }
            }
        });
    }

    public void getUserDataBy(String social_id, final OnServerResponseListener<User> listener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("StepUser");
        query.whereEqualTo("social_id", social_id);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (listener == null) return;

                if (scoreList != null && !scoreList.isEmpty()) {
                    User u = new User();
                    listener.onReady(u.toUser(scoreList.get(0)), true);
                } else {
                    listener.onReady(null, false);
                }
            }
        });
    }

    public void getAchievementList(final OnServerResponseListener<List<Achievement>> responseListener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Achievements");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> achievementList, ParseException e) {
                List<Achievement> achievements = new ArrayList<>();

                if (e == null) {
                    for (ParseObject obj : achievementList) {
                        achievements.add(new Achievement(obj));
                    }
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }

                if (responseListener == null) return;

                responseListener.onReady(achievements, e == null);
            }
        });
    }

    public void getDayList(String user_social_id, final OnServerResponseListener<List<Day>> responseListener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Date");
        query.whereEqualTo("user_social_id", user_social_id);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> daylist, ParseException e) {
                List<Day> dataList = new ArrayList<>();
                if (e == null) {
                    for (ParseObject obj : daylist) {
                        dataList.add(new Day(obj));

                    }
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }

                if (responseListener == null) return;

                responseListener.onReady(dataList,e == null);
            }
        });
    }

    public void getBadgeList(String user_social_id, final OnServerResponseListener<List<Badge>> responseListener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UsersBadges");
        query.whereEqualTo("user_social_id", user_social_id);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> achievementList, ParseException e) {
                List<Badge> badges = new ArrayList<>();
                if (e == null) {
                    for (ParseObject obj : achievementList) {
                        badges.add(new Badge(obj));
                    }
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }

                if (responseListener == null) return;

                responseListener.onReady(badges,e == null);
            }
        });
    }

    public void getStepList(String user_social_id, long time_ts, final OnServerResponseListener<List<StepPoint>> listener) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("StepPoint");
        query.whereEqualTo("user_social_id", user_social_id);
        query.whereGreaterThanOrEqualTo("created_at", time_ts);
        query.whereLessThan("created_at", time_ts + 86400000);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> steps, ParseException e) {
                List<StepPoint> stepPoints = new ArrayList<>();
                if (e == null) {
                    for (ParseObject obj : steps) {
                        stepPoints.add(new StepPoint(obj));
                    }
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }

                if (listener == null) return;

                listener.onReady(stepPoints,e == null);
            }
        });
    }

    /**
     * Start a new challenge.
     * @param challenge
     */
    public void startNewChallenge(Challenge challenge) {
        challenge.toParseObject().saveInBackground();
    }

    public void getChallengeByChallengeId(String challengeId, final OnServerResponseListener<Challenge> listener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Challange");
        query.whereEqualTo("challange_id", challengeId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> parse_challenge, ParseException e) {
                Challenge challenge = null;
                if (e == null) {
                    challenge = new Challenge(parse_challenge.get(0));

                } else {
                    Log.d("Challenge", "Error: " + e.getMessage());
                }

                if (listener != null) {
                    listener.onReady(challenge,e == null);
                }
            }
        });
    }

    public void getChallengeById(String id, final OnServerResponseListener<List<Challenge>> listener) {

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        ParseQuery<ParseObject> owner = ParseQuery.getQuery("Challange");
        owner.whereEqualTo("owner_id", id);

        ParseQuery<ParseObject> op_1 = ParseQuery.getQuery("Challange");
        op_1.whereEqualTo("opoment_one_id", id);

        ParseQuery<ParseObject> op_2 = ParseQuery.getQuery("Challange");
        op_2.whereEqualTo("opoment_two_id", id);

        ParseQuery<ParseObject> op_3 = ParseQuery.getQuery("Challange");
        op_3.whereEqualTo("opoment_three_id", id);

        queries.add(owner);
        queries.add(op_1);
        queries.add(op_2);
        queries.add(op_3);

        ParseQuery<ParseObject> q = ParseQuery.or(queries);
        q.whereGreaterThanOrEqualTo("duration",System.currentTimeMillis());

        q.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> challenge, ParseException e) {

                List<Challenge> challengeList = new ArrayList<Challenge>();
                if (e == null) {
                    for (ParseObject o : challenge) {
                        challengeList.add(new Challenge(o));

                    }
                } else {
                    Log.d("Challenge", "Error: " + e.getMessage());
                }

                if (listener != null) {
                    listener.onReady(challengeList,e == null);
                }
            }
        });
    }

    public void acceptChallengeRequest(String challengeId, final String user_id, final OnServerResponseListener<Boolean> listener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Challange");
        query.whereEqualTo("challange_id", challengeId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> challengeList, ParseException e) {
                boolean isAcceptSuccessful = false;
                if (challengeList != null && !challengeList.isEmpty()) {
                    ParseObject object = challengeList.get(0);
                    Challenge challenge = new Challenge(object);

                    if (challenge.opoment_one_id.equalsIgnoreCase("empty")) {
                        isAcceptSuccessful = true;
                        challenge.opoment_one_id = user_id;
                    } else if (challenge.opoment_two_id.equalsIgnoreCase("empty")) {
                        isAcceptSuccessful = true;
                        challenge.opoment_two_id = user_id;
                    } else if (challenge.opoment_three_id.equalsIgnoreCase("empty")) {
                        isAcceptSuccessful = true;
                        challenge.opoment_three_id = user_id;
                    }

                    if (isAcceptSuccessful) {
                        NotificationManager.getInstance().setNotificationForChallenge(challenge);
                    }

                    challenge.toParseObject().saveInBackground();
                }

                if (listener != null) {
                    listener.onReady(isAcceptSuccessful,e == null);
                }
            }
        });
    }

    public void updateUserData(final User user) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("StepUser");
        query.whereEqualTo("social_id", user.social_id);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {

                if (scoreList != null && !scoreList.isEmpty()) {
                    ParseObject object = scoreList.get(0);

                    object.put("social_id",user.social_id);
                    object.put("first_name",user.first_name);
                    object.put("last_name",user.last_name);
                    object.put("email",user.email == null ? "" : user.email);
                    object.put("picture_url",user.picture_url);
                    object.put("login_type",user.login_type);
                    object.put("step_count",user.steps_count);
                    object.put("latitude",user.latitude + "");
                    object.put("longitude",user.longitude + "");

                    object.saveInBackground();
                }
            }
        });
    }
public void updateChallenge(final Challenge challenge) {
    ParseQuery<ParseObject> query = ParseQuery.getQuery("Challange");
    query.whereEqualTo("challange_id", challenge.challange_id);
    query.whereEqualTo("winner_id", "empty");
    query.findInBackground(new FindCallback<ParseObject>() {
        public void done(List<ParseObject> scoreList, ParseException e) {

            if (scoreList != null && !scoreList.isEmpty()) {
                ParseObject request = scoreList.get(0);
                request.put("challange_id",challenge.challange_id);
                request.put("owner_id",challenge.owner_id);
                request.put("winner_id",challenge.winner_id == null ? "empty" : challenge.winner_id);
                request.put("color",challenge.color);
                request.put("lat",challenge.lat + "");
                request.put("lng",challenge.lng + "");
                request.put("opoment_one_id",challenge.opoment_one_id == null ? "empty" : challenge.opoment_one_id);
                request.put("opoment_two_id",challenge.opoment_two_id == null ? "empty" : challenge.opoment_two_id);
                request.put("opoment_three_id",challenge.opoment_three_id == null ? "empty" : challenge.opoment_three_id);
                request.put("duration",challenge.duration);
                request.put("type",challenge.type);
                request.put("title",challenge.title);
                request.put("message",challenge.message);
                request.put("bet",challenge.bet);

                request.saveInBackground();
            }
        }
    });
}

    public interface OnServerResponseListener<TResponseObject> {
        public void onReady(TResponseObject response, boolean isSuccess);
    }
}
