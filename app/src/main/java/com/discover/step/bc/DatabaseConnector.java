package com.discover.step.bc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.discover.step.Config;
import com.discover.step.R;
import com.discover.step.StepApplication;
import com.discover.step.ex.DefaultStepException;
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
        } catch (SQLException e) {
            if (Config.IS_DEVELOPER_MODE) {
                Log.d("Can't create database", e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i2) {
        //Not used yet.
    }

    public void setUser(final User user) throws DefaultStepException {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Dao<User, ?> dao = getDao(User.class);
                    dao.createIfNotExists(user);
                    return null;
                }
            });
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("SET User Database operation failed");
        }
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

    public List<StepPoint> getStepPoints() throws DefaultStepException {
        List<StepPoint> stepPointList = null;
        try {
            stepPointList = getDao(StepPoint.class).queryBuilder().orderBy("created_at",true).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET STEP POINTS Database operation failed");
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
