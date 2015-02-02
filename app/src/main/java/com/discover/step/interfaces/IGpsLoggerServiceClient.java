package com.discover.step.interfaces;

import android.location.Location;

import com.discover.step.model.StepPoint;

/**
 * Created by Geri on 2015.01.18..
 */
public interface IGpsLoggerServiceClient {

    public void onNewStepPointsAvailable(StepPoint stepPoint);
    public void onMainPositionChange(Location stepPoint);

}
