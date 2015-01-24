package com.discover.step.bl;

import com.discover.step.bc.ServerConnector;
import com.discover.step.model.StepPoint;
import com.discover.step.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geri on 2015.01.20..
 */
public class LocationStoreProxy {

    private static LocationStoreProxy mInstance = null;

    private List<StepPoint> proxyList;
    private User currentUser;

    private static final int STORED_STEP = 15;

    public static LocationStoreProxy getInstance() {
        if (mInstance == null) {
            mInstance = new LocationStoreProxy();
        }
        return mInstance;
    }

    private LocationStoreProxy() {
        proxyList = new ArrayList<>();
        currentUser = UserManager.getInstance().getAuthenticatedUser();
    }

    public void insertStepPoint(StepPoint stepPoint) {
        stepPoint.user_social_id = currentUser.social_id;
        if (proxyList.size() < STORED_STEP) {
            proxyList.add(stepPoint);
        } else {
            proxyList.add(stepPoint);
            //store point in local.
            StepManager.getInstance().setStepPoints(proxyList);
            //send point to server db.
            ServerConnector.getInstance().sendStepPoints(proxyList);
            proxyList = new ArrayList<>();
        }
    }

    public void forceOfStoreStepPoints() {
        if (!proxyList.isEmpty()) {
            //store point in local.
            StepManager.getInstance().setStepPoints(proxyList);
            //send point to server db.
            ServerConnector.getInstance().sendStepPoints(proxyList);
        }
    }
}
