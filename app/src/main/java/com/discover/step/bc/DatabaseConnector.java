package com.discover.step.bc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.discover.step.Config;
import com.discover.step.R;
import com.discover.step.StepApplication;
import com.discover.step.ex.DefaultStepException;
import com.discover.step.model.StepPoint;
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

    public List<StepPoint> getStepPoints() throws DefaultStepException {
        List<StepPoint> stepPointList = null;
        try {
            stepPointList = getDao(StepPoint.class).queryBuilder().orderBy("created_at",true).query();
        } catch (java.sql.SQLException e) {
            throw new DefaultStepException("GET STEP POINTS Database operation failed");
        }
        return stepPointList;
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
}
