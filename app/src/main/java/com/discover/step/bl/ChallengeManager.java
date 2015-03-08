package com.discover.step.bl;

import android.location.Location;
import android.util.Log;

import com.discover.step.Session;
import com.discover.step.async.GPSTrackerService;
import com.discover.step.bc.DatabaseConnector;
import com.discover.step.bc.ServerConnector;
import com.discover.step.ex.DefaultStepException;
import com.discover.step.model.Challenge;
import com.discover.step.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geri on 2015.03.02..
 */
public class ChallengeManager {

    private static ChallengeManager mInstance = null;
    private OnChallengeSynced mSyncListener;

    public static ChallengeManager getInstance() {
        if (mInstance == null) {
            mInstance = new ChallengeManager();
        }
        return mInstance;
    }

    public void setOnChallengeSyncListener(OnChallengeSynced listener) {
        this.mSyncListener = listener;
    }

    public void createNewChallenge(Challenge challenge) {
        try {
            ServerConnector.getInstance().startNewChallenge(challenge);
            NotificationManager.getInstance().setNotificationForChallenge(challenge);
            List<Challenge> challenges = new ArrayList<>();
            challenges.add(challenge);
            DatabaseConnector.getInstance().setChallengeList(challenges);
        } catch (DefaultStepException e) {
            e.printStackTrace();
        }
    }

    public void updateChallenge(Challenge challenge) {
        try {
            DatabaseConnector.getInstance().updateChallenge(challenge);
            ServerConnector.getInstance().updateChallenge(challenge);
        } catch (DefaultStepException e) {
            Log.w("UserManager", "Update user Exception: " + e.getMessage());
        }
    }

    public void downloadChallengeByUserId(String userId) {
        ServerConnector.getInstance().getChallengeById(userId, new ServerConnector.OnServerResponseListener<List<Challenge>>() {
            @Override
            public void onReady(List<Challenge> response, boolean isSuccess) {
                try {
                    DatabaseConnector.getInstance().updateChallenge(response);

                    for (Challenge c : response) {
                        getChallengerUsers(c);
                    }

                } catch (DefaultStepException e) {
                    Log.d(ChallengeManager.class.getName(),"Error: " + e.getMessage());
                }

                if (mSyncListener != null)
                    mSyncListener.onSynced();
            }
        });
    }

    private void getChallengerUsers(Challenge challenge) {
        String[] ids = new String[] {challenge.owner_id, challenge.opoment_one_id, challenge.opoment_two_id, challenge.opoment_three_id};
        for (String id : ids) {
            if (!id.equalsIgnoreCase("empty") && !id.equalsIgnoreCase(Session.getAuthenticatedUserSocialId())) {
                ServerConnector.getInstance().getUserDataBy(id, new ServerConnector.OnServerResponseListener<User>() {
                    @Override
                    public void onReady(User response, boolean isSuccess) {
                        try {
                            if (isSuccess)
                                DatabaseConnector.getInstance().setUser(response);

                        } catch (DefaultStepException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public void downloadChallengeByChallengeId(final String challengeId, final OnReady<Challenge> listener) {
        ServerConnector.getInstance().getChallengeByChallengeId(challengeId, new ServerConnector.OnServerResponseListener<Challenge>() {
            @Override
            public void onReady(Challenge response, boolean isSuccess) {
                try {
                    List<Challenge> challenges = new ArrayList<Challenge>();
                    challenges.add(response);
                    DatabaseConnector.getInstance().setChallengeList(challenges);
                    getChallengerUsers(response);

                } catch (DefaultStepException e) {
                    Log.d(ChallengeManager.class.getName(),"Error: " + e.getMessage());
                }

                if (listener != null && isSuccess)
                    listener.onReady(response);
            }
        });
    }

    public void acceptChallenge(String challengeId, final OnReady<Boolean> onReadyListener) {
        String userId = UserManager.getInstance().getAuthenticatedUser().social_id;
        ServerConnector.getInstance().acceptChallengeRequest(challengeId, userId, new ServerConnector.OnServerResponseListener<Boolean>() {
            @Override
            public void onReady(Boolean response, boolean isSuccess) {
                if (onReadyListener != null) {
                    onReadyListener.onReady(response);
                }
            }
        });
    }

    public List<Challenge> getMyChallenges() {
        try {
            return DatabaseConnector.getInstance().getChallengeByUserId(Session.getAuthenticatedUserSocialId());
        } catch (DefaultStepException e) {
            Log.e(ChallengeManager.class.getName(),"Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void deleteExpiredChallenge() {
        try {
            DatabaseConnector.getInstance().deleteExpiredChallenge();
        } catch (DefaultStepException e) {
            e.printStackTrace();
        }
    }

    public void checkGameState(Location location) {
        List<Challenge> challenges = getMyChallenges();

        for (Challenge c : challenges) {
            if (c.type == 0) {
                User op1 = UserManager.getInstance().getUserBySocialId(c.opoment_one_id);
                User op2 = UserManager.getInstance().getUserBySocialId(c.opoment_two_id);
                User op3 = UserManager.getInstance().getUserBySocialId(c.opoment_three_id);

                if (isUserWin(op1,location)) {
                    notifyChallenge(c,"Challenge is over! " + op1.first_name + " " + op1.last_name + " win the game!",Session.getAuthenticatedUserSocialId());
                } else if (isUserWin(op2,location)) {
                    notifyChallenge(c,"Challenge is over! " + op2.first_name + " " + op2.last_name + " win the game!",Session.getAuthenticatedUserSocialId());
                } else if (isUserWin(op3,location)) {
                    notifyChallenge(c,"Challenge is over! " + op3.first_name + " " + op3.last_name + " win the game!",Session.getAuthenticatedUserSocialId());
                }

            } else {
                handleCatchEvent(c,location);
            }
        }
    }

    private boolean isUserWin(User user, Location location) {
        if (user != null) {
            Location userLocation = new Location("user");
            userLocation.setLatitude(user.latitude);
            userLocation.setLongitude(user.longitude);

            return distanceFromLastPoint(location,userLocation) < 3;
        }
        return false;
    }

    private void handleCatchEvent(Challenge c, Location location) {
        Location placeLocation = new Location("place");
        placeLocation.setLatitude(c.lat);
        placeLocation.setLongitude(c.lng);

        if (distanceFromLastPoint(location,placeLocation) < 3) {
            notifyChallenge(c, "Congratulation! You have won this challenge!", Session.getAuthenticatedUserSocialId());
        }
    }

    private void notifyChallenge(Challenge c, String message, String userId) {
        NotificationManager.getInstance().removeNotificationForFavourite(c);
        NotificationManager.getInstance().showChallengeNotification(c,message);
        c.winner_id = userId;
        c.isChallengeOver = true;
        updateChallenge(c);
    }


    private int distanceFromLastPoint(Location location1, Location location2) {
        return (int) location1.distanceTo(location2);
    }


    public interface OnReady<TValue> {
        public void onReady(TValue data);
    }

    public interface OnChallengeSynced {
        public void onSynced();
    }
}
