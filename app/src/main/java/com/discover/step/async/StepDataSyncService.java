package com.discover.step.async;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.discover.step.Config;
import com.discover.step.Session;
import com.discover.step.bc.DatabaseConnector;
import com.discover.step.bc.ServerConnector;
import com.discover.step.bl.UserManager;
import com.discover.step.ex.DefaultStepException;
import com.discover.step.model.Day;
import com.discover.step.model.StepPoint;
import com.discover.step.model.User;

import java.util.List;

/**
 * Created by Geri on 2015.01.29..
 */
public class StepDataSyncService extends IntentService {

    private AlarmManager mAlarmManager;
    List<Day> unSyncedDays;
    List<StepPoint> unSyncedStepPoints;

    public StepDataSyncService() {
        super("StepDataSyncService");
    }

    @Override
    protected synchronized void onHandleIntent(Intent intent) {
        mAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        try {
//            //Get all of un syced step points.
////            unSyncedStepPoints = DatabaseConnector.getInstance().getUnSyncedStepPoints(Session.authenticated_user_social_id);
////            unSyncedDays = DatabaseConnector.getInstance().getUnSyncedDay(Session.authenticated_user_social_id);
////
////            if (!unSyncedStepPoints.isEmpty()) {
////                //Sync of step points.
////                ServerConnector.getInstance().sendStepPoints(unSyncedStepPoints);
////            }
////
////            if (!unSyncedDays.isEmpty()) {
////                //Sync of days.
////                ServerConnector.getInstance().sendDays(unSyncedDays);
////            }
//        } catch (DefaultStepException e) {
//            e.printStackTrace();
//            Log.d("StepPointsService","fail");
//            mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Config.UPDATE_SERVER_DATA_SYNC_ALARM_TRIGGER_AT_MILLIS, getSyncPendingIntent(StepDataSyncService.this));
//        }

//        mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Config.UPDATE_SERVER_DATA_SYNC_ALARM_TRIGGER_AT_MILLIS, getSyncPendingIntent(StepDataSyncService.this));
    }

    private PendingIntent getSyncPendingIntent(Context context) {
        PendingIntent syncServicePendingIntent = PendingIntent.getService(context, 0, new Intent(context, StepDataSyncService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        return syncServicePendingIntent;
    }
}
