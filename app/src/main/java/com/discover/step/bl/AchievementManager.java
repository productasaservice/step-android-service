package com.discover.step.bl;

import android.util.Log;

import com.discover.step.Session;
import com.discover.step.bc.DatabaseConnector;
import com.discover.step.bc.ServerConnector;
import com.discover.step.ex.DefaultStepException;
import com.discover.step.model.Achievement;
import com.discover.step.model.Badge;
import com.discover.step.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geri on 2015.01.25..
 */
public class AchievementManager {
    private static AchievementManager mInstance = null;

    public static AchievementManager getInstance() {
        if (mInstance == null) {
            mInstance = new AchievementManager();
        }
        return mInstance;
    }

    public void downloadAchievements() {
        ServerConnector.getInstance().getAchievementList(new ServerConnector.OnServerResponseListener<List<Achievement>>() {
            @Override
            public void onReady(List<Achievement> achievements, boolean isSuccess) {
                if (isSuccess) {
                    try {
                        DatabaseConnector.getInstance().setAchievementList(achievements);
                    } catch (DefaultStepException e) {
                        Log.w("AchievementManager", "Set Achievements Exception: " + e.getMessage());
                    }
                }
            }
        });
    }

    public void downloadBadges() {
        if (Session.authenticated_user_social_id == null)    Session.start();

        ServerConnector.getInstance().getBadgeList(Session.authenticated_user_social_id,new ServerConnector.OnServerResponseListener<List<Badge>>() {
            @Override
            public void onReady(List<Badge> badges, boolean isSuccess) {
                if (isSuccess) {
                    try {
                        DatabaseConnector.getInstance().setBadgesList(badges);
                    } catch (DefaultStepException e) {
                        Log.w("AchievementManager", "Set Achievements Exception: " + e.getMessage());
                    }
                }
            }
        });
    }

    public List<Achievement> getAchievements() {
        try {
            return DatabaseConnector.getInstance().getAchievementList();
        } catch (DefaultStepException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Achievement checkForNewBadge() {
        return stepPointBasedBadge();
    }

    private Achievement stepPointBasedBadge() {
        try {
            if (Session.authenticated_user_social_id == null)    Session.start();

            int step_count = PrefManager.getInstance().getUserStepCount(Session.authenticated_user_social_id);
            List<Achievement> achievements = DatabaseConnector.getInstance().getAchievementList();
            for (Achievement a : achievements) {

                if (!isStepBasedAchievementCompleted(a) && (a.goal <= step_count)) {
                    Badge badge = new Badge();
                    badge.created_at = System.currentTimeMillis();
                    badge.user_social_id = Session.authenticated_user_social_id;
                    badge.achievement_id = a.achievement_id;

                    DatabaseConnector.getInstance().setBadge(badge);
                    ServerConnector.getInstance().sendBadge(badge);
                    return a;
                }
            }

        } catch (DefaultStepException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isStepBasedAchievementCompleted(Achievement achievement) {
        try {
            return DatabaseConnector.getInstance().getBadge(achievement.achievement_id) != null;
        } catch (DefaultStepException e) {
            e.printStackTrace();
            return false;
        }
    }
}
