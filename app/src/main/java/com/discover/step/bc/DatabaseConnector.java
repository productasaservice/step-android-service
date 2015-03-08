package com.discover.step.bc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.discover.step.Config;
import com.discover.step.R;
import com.discover.step.Session;
import com.discover.step.StepApplication;
import com.discover.step.ex.DefaultStepException;
import com.discover.step.model.Achievement;
import com.discover.step.model.Badge;
import com.discover.step.model.Challenge;
import com.discover.step.model.Day;
import com.discover.step.model.StepPoint;
import com.discover.step.model.User;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Geri on 2015.01.18..
 */
public class DatabaseConnector extends OrmLiteSqliteOpenHelper {

    private static DatabaseConnector mInstance;

    public static DatabaseConnector getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseConnector(StepApplication.getContext());
        }
        return mInstance;
    }

    private DatabaseConnector(Context context) {
        super(context, Config.DB_NAME, null, Config.DB_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, StepPoint.class);
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, Badge.class);
            TableUtils.createTable(connectionSource, Achievement.class);
            TableUtils.createTable(connectionSource, Day.class);
            TableUtils.createTable(connectionSource, Challenge.class);
        } catch (SQLException e) {
            if (Config.IS_DEVELOPER_MODE) {
                Log.d("Can't create database", e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int old_version, int new_version) {
        //Not used yet.
        try {
            if (old_version == 1 && new_version == 2) {
                sqLiteDatabase.execSQL("ALTER TABLE 'user' ADD steps_count INTEGER");
                sqLiteDatabase.execSQL("ALTER TABLE 'steppoint' ADD isSynced INTEGER");
                //create a new table.
                TableUtils.createTable(connectionSource, Day.class);
            } else if (old_version == 2 && new_version == 3) {
                sqLiteDatabase.execSQL("ALTER TABLE 'user' ADD latitude REAL");
                sqLiteDatabase.execSQL("ALTER TABLE 'user' ADD longitude REAL");
                //create a new table.
                TableUtils.createTable(connectionSource, Challenge.class);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set create new user if not exists.
     * @param user
     * @throws DefaultStepException
     */
    public void setUser(final User user) throws DefaultStepException {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Dao<User, ?> dao = getDao(User.class);
                    User u = getUserBySocialId(user.social_id);
                    if (u != null) {
                        user.id = u.id;
                        dao.update(user);
                    } else {
                        dao.create(user);
                    }
                    return null;
                }
            });
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("SET User Database operation failed");
        }
    }

    /**
     * Set achievement list.
     * @param achievementList
     * @throws DefaultStepException
     */
    public void setAchievementList(final List<Achievement> achievementList) throws DefaultStepException {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Dao<Achievement, ?> dao = getDao(Achievement.class);
                    dao.deleteBuilder().delete();
                    for (Achievement a : achievementList) {
                        dao.createIfNotExists(a);
                    }
                    return null;
                }
            });
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("SET Achievement Database operation failed");
        }
    }

    public void setBadge(final Badge badge) throws DefaultStepException {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Dao<Badge, ?> dao = getDao(Badge.class);
                    dao.createIfNotExists(badge);
                    return null;
                }
            });
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("SET Badge Database operation failed");
        }
    }

    /**
     * Set badge list.
     * @param badgeList
     * @throws DefaultStepException
     */
    public void setBadgesList(final List<Badge> badgeList) throws DefaultStepException {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Dao<Badge, ?> dao = getDao(Badge.class);
                    for (Badge a : badgeList) {
                        dao.createIfNotExists(a);
                    }
                    return null;
                }
            });
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("SET Badges Database operation failed");
        }
    }

    /**
     * Set badge list.
     * @param challengeList
     * @throws DefaultStepException
     */
    public void setChallengeList(final List<Challenge> challengeList) throws DefaultStepException {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Dao<Challenge, ?> dao = getDao(Challenge.class);
                    for (Challenge a : challengeList) {
                        dao.createIfNotExists(a);
                    }
                    return null;
                }
            });
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("SET Challenge Database operation failed with error: " + e.getMessage());
        }
    }

    /**
     * Set day list to database.
     * @param dayList
     * @throws DefaultStepException
     */
    public void setDayList(final List<Day> dayList) throws DefaultStepException {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Dao<Day, ?> dao = getDao(Day.class);
                    for (Day a : dayList) {
                        dao.createIfNotExists(a);
                    }
                    return null;
                }
            });
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("SET Day Database operation failed");
        }
    }

    /**
     * set or update day
     * @param day
     * @throws DefaultStepException
     */
    public void setOrUpdateDay(final Day day) throws DefaultStepException {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Dao<Day, ?> dao = getDao(Day.class);
                    if (getDayByDayId(day.day_id) == null) {
                        dao.createIfNotExists(day);
                    } else {
                        dao.update(day);
                    }
                    return null;
                }
            });
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("SET/UPDATE day database operation failed");
        }
    }

    public Day getDayByDayId(String day_id) throws DefaultStepException {
        List<Day> day = null;
        try {
            day = getDao(Day.class).queryBuilder().where().eq("day_id",day_id).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET day database operation failed");
        }
        return day != null && !day.isEmpty() ? day.get(0) :  null;
    }

    public Challenge getChallengeByChallengeId(String challengeId) throws DefaultStepException {
        List<Challenge> challenge = null;
        try {
            challenge = getDao(Challenge.class).queryBuilder().where().eq("challange_id",challengeId).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET Challenge of User Database operation failed");
        }
        return challenge != null && !challenge.isEmpty() ? challenge.get(0) :  null;
    }

    public List<Challenge> getChallengeByUserId(String userId) throws DefaultStepException {
        List<Challenge> challenge = null;
        try {
            challenge = getDao(Challenge.class).queryBuilder().where().ge("duration", System.currentTimeMillis()).and().eq("owner_id", userId).or().eq("opoment_one_id",userId).or().eq("opoment_two_id",userId).or().eq("opoment_three_id",userId).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET Challenge of User Database operation failed");
        }
        return challenge;
    }

    public void updateChallenge(final List<Challenge> challenges) throws DefaultStepException {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Dao<Challenge, Long> dao = getDao(Challenge.class);
                    for (Challenge ch : challenges) {
                        Challenge oldTrack = getChallengeByChallengeId(ch.challange_id);
                        if (oldTrack != null) {
                            ch.isChallengeRequestNoticed = oldTrack.isChallengeRequestNoticed;
                            ch.isChallengeOver = oldTrack.isChallengeOver;
                        }
                        ch.isDeleted = false;

                        dao.createOrUpdate(ch);
                        Challenge o = getChallengeByChallengeId(ch.challange_id);
                    }
                    return null;
                }
            });
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("Update Challenges of User Database operation failed with error: " + e.getMessage());
        }
        deleteUnusedChallenge();
    }

    public void deleteUnusedChallenge() throws DefaultStepException {
        try {
            Dao<Challenge, ?> dao = getDao(Challenge.class);
            List<Challenge> challengeList = getChallengeByUserId(Session.getAuthenticatedUserSocialId());

            for (Challenge ch : challengeList) {
                if (ch.isDeleted == true || ch.duration < System.currentTimeMillis()) {
                    dao.delete(ch);
                } else {
                    ch.isDeleted = true;
                    dao.update(ch);
                }
            }
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("Delete Challenges with error: " + e.getMessage());
        }
    }

    public void deleteExpiredChallenge() throws DefaultStepException {
        try {
            Dao<Challenge, ?> dao = getDao(Challenge.class);
            List<Challenge> challengeList = getChallengeByUserId(Session.getAuthenticatedUserSocialId());

            for (Challenge ch : challengeList) {
                if (ch.duration < System.currentTimeMillis()) {
                    dao.delete(ch);
                }
            }
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("Delete Challenges with error: " + e.getMessage());
        }
    }

    public List<Day> getDayList(String social_id) throws DefaultStepException {
        List<Day> day = null;
        try {
            day = getDao(Day.class).queryBuilder().orderBy("date_ts",true).where().eq("social_id",social_id).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET day list database operation failed");
        }
        return day;
    }

    public Badge getBadge(String achievement_id) throws DefaultStepException {
        List<Badge> badge = null;
        try {
            badge = getDao(Badge.class).queryBuilder().where().eq("achievement_id",achievement_id).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET Badge of User Database operation failed");
        }
        return badge != null && !badge.isEmpty() ? badge.get(0) :  null;
    }

    public List<Achievement> getAchievementList() throws DefaultStepException {
        List<Achievement> achievements = null;
        try {
            achievements = getDao(Achievement.class).queryBuilder().orderBy("goal",true).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET Logged in User Database operation failed");
        }
        return achievements;
    }

    public User getUserBySocialId(String id) throws DefaultStepException {
        List<User> users = null;
        try {
            users = getDao(User.class).queryBuilder().where().eq("social_id", id).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET Logged in User Database operation failed");
        }
        return users != null && !users.isEmpty() ? users.get(0) : null;
    }

    public User getLoggedInUser() throws DefaultStepException {
        List<User> users = null;
        try {
            users = getDao(User.class).queryBuilder().where().eq("isLoggedIn", true).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET Logged in User Database operation failed");
        }
        return users != null && !users.isEmpty() ? users.get(0) : null;
    }

    public void updateUser(User user) throws DefaultStepException {
        try {
            getDao(User.class).update(user);
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("UPDATE User Database operation failed");
        }
    }

    public void updateChallenge(Challenge challenge) throws DefaultStepException {
        try {
            getDao(Challenge.class).update(challenge);
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("UPDATE Challenge Database operation failed");
        }
    }

    public void setStepPoints(final List<StepPoint> stepPoints) throws DefaultStepException {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Dao<StepPoint, ?> dao = getDao(StepPoint.class);

                    for (StepPoint sp : stepPoints) {
                        dao.createIfNotExists(sp);
                    }
                    return null;
                }
            });
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("SET STEP POINTS Database operation failed");
        }
    }

    public List<StepPoint> getStepPoints(String social_id) throws DefaultStepException {
        List<StepPoint> stepPointList = null;
        try {
            stepPointList = getDao(StepPoint.class).queryBuilder().orderBy("created_at",true).where().eq("user_social_id",social_id).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET STEP POINTS Database operation failed");
        }
        return stepPointList;
    }

    public List<StepPoint> getStepPoints(long time, String user_social_id) throws DefaultStepException {
        List<StepPoint> stepPointList = null;
        try {
            stepPointList = getDao(StepPoint.class).queryBuilder().orderBy("created_at",true).where().eq("user_social_id",user_social_id).and().ge("created_at",time).and().lt("created_at",time + 86400000).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET STEP POINTS Database operation failed");
        }
        return stepPointList;
    }

    public List<StepPoint> getUnSyncedStepPoints(String user_social_id) throws DefaultStepException {
        List<StepPoint> stepPointList = null;
        try {
            stepPointList = getDao(StepPoint.class).queryBuilder().where().eq("user_social_id",user_social_id).and().eq("isSynced",false).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET STEP POINTS Database operation failed: " + e.getMessage());
        }
        return stepPointList;
    }

    public List<Day> getUnSyncedDay(String user_social_id) throws DefaultStepException {
        List<Day> stepPointList = null;
        try {
            stepPointList = getDao(Day.class).queryBuilder().where().eq("social_id",user_social_id).and().eq("isSynced",false).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET day Database operation failed");
        }
        return stepPointList;
    }

    public List<StepPoint> getNotDrawnStepPoints() throws DefaultStepException {
        List<StepPoint> stepPointList = null;
        try {
            stepPointList = getDao(StepPoint.class).queryBuilder().orderBy("created_at",false).where().eq("isVisibleOnMap",false).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET STEP POINTS Database operation failed");
        }
        return stepPointList;
    }

    public void updateStepPoints(final List<StepPoint> points) throws DefaultStepException {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Dao<StepPoint, ?> dao = getDao(StepPoint.class);
                    for (StepPoint point : points) {
                        dao.createOrUpdate(point);
                    }
                    return null;
                }
            });
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("Update POINTS Database operation failed");
        }
    }
}
