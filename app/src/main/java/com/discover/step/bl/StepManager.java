package com.discover.step.bl;

import android.util.Log;

import com.discover.step.Session;
import com.discover.step.StepApplication;
import com.discover.step.bc.DatabaseConnector;
import com.discover.step.bc.ServerConnector;
import com.discover.step.ex.DefaultStepException;
import com.discover.step.model.Day;
import com.discover.step.model.StepPoint;
import com.discover.step.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geri on 2015.01.18..
 */
public class StepManager {

    private static StepManager mInstance;

    public static StepManager getInstance() {
        if (mInstance == null) {
            mInstance = new StepManager();
        }
        return mInstance;
    }

    public void downloadDayList() {
        if (Session.authenticated_user_social_id == null)
            Session.start();

        ServerConnector.getInstance().getDayList(Session.authenticated_user_social_id, new ServerConnector.OnServerResponseListener<List<Day>>() {
            @Override
            public void onReady(List<Day> response, boolean isSuccess) {
                    try {
                        if (isSuccess)
                            DatabaseConnector.getInstance().setDayList(response);
                    } catch (DefaultStepException e) {
                        e.printStackTrace();
                    }
            }
        });
    }

    public void setStepPoint(StepPoint stepPoint) {
        if (stepPoint != null) {
            try {
                List<StepPoint> step_point = new ArrayList<>();
                step_point.add(stepPoint);
                DatabaseConnector.getInstance().setStepPoints(step_point);
                //Store day data.
                addNewDayIfNeeded();
            } catch (DefaultStepException e) {
                Log.w("StepManager", "Set Step Point Exception: " + e.getMessage());
            }
        }
    }

    public void setStepPoints(List<StepPoint> stepPoints) {
        if (!stepPoints.isEmpty()) {
            try {
                DatabaseConnector.getInstance().setStepPoints(stepPoints);
                //Store day data.
                addNewDayIfNeeded();

            } catch (DefaultStepException e) {
                Log.w("StepManager", "Set Step Point Exception: " + e.getMessage());
            }
        }
    }

    private void addNewDayIfNeeded() {
        Day day = new Day();
        day.create();

        if (day.day_id != null) {
            setOrUpdateDay(day);
        }
    }

    public List<StepPoint> getStepPoints() {
        try {
            if (Session.authenticated_user_social_id == null) Session.start();

            return DatabaseConnector.getInstance().getStepPoints(Session.authenticated_user_social_id);
        } catch (DefaultStepException e) {
            Log.w("StepManager", "Get Step Points Exception: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<StepPoint> getStepPointsByTimeStamp(long time) {
        try {
            return DatabaseConnector.getInstance().getStepPoints(time, Session.authenticated_user_social_id);
        } catch (DefaultStepException e) {
            Log.w("StepManager", "Get Step Points Exception: " + e.getMessage());
            return new ArrayList<>();
        } catch (NullPointerException e) {
            Log.w("StepManager", "Get Step Points Exception: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<StepPoint> getNotDrawnStepPoints() {
        try {
            return DatabaseConnector.getInstance().getNotDrawnStepPoints();
        } catch (DefaultStepException e) {
            Log.w("StepManager", "Get Step Points Exception: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void updateStepPoints(List<StepPoint> points) {
        try {
            DatabaseConnector.getInstance().updateStepPoints(points);
        } catch (DefaultStepException e) {
            Log.w("StepManager", "Update Step Points Exception: " + e.getMessage());
        }
    }

    public List<Day> getStepDays() {
        try {
            if (Session.authenticated_user_social_id == null) {
                Session.start();
            }

            return DatabaseConnector.getInstance().getDayList(Session.authenticated_user_social_id);
        } catch (DefaultStepException e) {
            //e.printStackTrace();
            return new ArrayList<>();
        } catch (NullPointerException e) {
            //e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void setOrUpdateDay(Day day) {
        try {
            DatabaseConnector.getInstance().setOrUpdateDay(day);
        } catch (DefaultStepException e) {
            Log.w("StepManager", "Update Day Exception: " + e.getMessage());
        }
    }
}
