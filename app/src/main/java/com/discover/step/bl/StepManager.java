package com.discover.step.bl;

import android.util.Log;

import com.discover.step.StepApplication;
import com.discover.step.bc.DatabaseConnector;
import com.discover.step.ex.DefaultStepException;
import com.discover.step.model.StepPoint;

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

    public void setStepPoint(StepPoint stepPoint) {
        if (stepPoint != null) {
            try {
                List<StepPoint> step_point = new ArrayList<>();
                step_point.add(stepPoint);
                DatabaseConnector.getInstance().setStepPoints(step_point);
            } catch (DefaultStepException e) {
                Log.w("StepManager", "Set Step Point Exception: " + e.getMessage());
            }
        }
    }

    public void setStepPoints(List<StepPoint> stepPoints) {
        if (!stepPoints.isEmpty()) {
            try {
                DatabaseConnector.getInstance().setStepPoints(stepPoints);
            } catch (DefaultStepException e) {
                Log.w("StepManager", "Set Step Point Exception: " + e.getMessage());
            }
        }
    }

    public List<StepPoint> getStepPoints() {
        try {
            return DatabaseConnector.getInstance().getStepPoints();
        } catch (DefaultStepException e) {
            Log.w("StepManager", "Get Step Points Exception: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
