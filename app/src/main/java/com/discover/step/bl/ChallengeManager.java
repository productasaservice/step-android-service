package com.discover.step.bl;

import com.discover.step.bc.ServerConnector;
import com.discover.step.model.Challenge;

import java.util.List;

/**
 * Created by Geri on 2015.03.02..
 */
public class ChallengeManager {

    private static ChallengeManager mInstance = null;

    public static ChallengeManager getInstance() {
        if (mInstance == null) {
            mInstance = new ChallengeManager();
        }
        return mInstance;
    }

    public void createNewChallenge(Challenge challenge) {
        ServerConnector.getInstance().startNewChallenge(challenge);
    }

    public void downloadChallengeByUserId(String userId) {
        ServerConnector.getInstance().getChallengeById(userId,new ServerConnector.OnServerResponseListener<List<Challenge>>() {
            @Override
            public void onReady(List<Challenge> response, boolean isSuccess) {

            }
        });
    }

    public void downloadChallengeByChallengeId(String challengeId) {
        ServerConnector.getInstance().getChallengeByChallengeId(challengeId,new ServerConnector.OnServerResponseListener<Challenge>() {
            @Override
            public void onReady(Challenge response, boolean isSuccess) {

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

    public interface OnReady<TValue> {
        public void onReady(TValue data);
    }
}
