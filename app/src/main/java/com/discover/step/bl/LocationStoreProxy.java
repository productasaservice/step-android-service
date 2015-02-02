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
    private long mLastInsertTime = 0;

    private static final int STORED_STEP = 10;

    public static LocationStoreProxy getInstance() {
        if (mInstance == null) {
            mInstance = new LocationStoreProxy();
        }
        return mInstance;
    }

    private LocationStoreProxy() {
        proxyList = new ArrayList<>();
        currentUser = UserManager.getInstance().getAuthenticatedUser();
        mLastInsertTime = System.currentTimeMillis();
    }

    /**
     * Insert a new point using proxy design patterns.
     * @param stepPoint
     */
    public void insertStepPoint(StepPoint stepPoint) {
        stepPoint.user_social_id = currentUser.social_id;
        stepPoint.isSynced = false;

        //Use proxy if we want to add points faster than 2 min.
        if (System.currentTimeMillis() - mLastInsertTime < 2000) {
            if (proxyList.size() < STORED_STEP) {
                proxyList.add(stepPoint);
            } else {
                proxyList.add(stepPoint);
                //Store all of step points on list.
                StepManager.getInstance().setStepPoints(proxyList);

                //Update user's data change.
                currentUser.steps_count = PrefManager.getInstance().getUserStepCount(currentUser.social_id);
                UserManager.getInstance().updateUser(currentUser);

                //Clear proxy.
                proxyList = new ArrayList<>();

                //Set last insert time.
                mLastInsertTime = System.currentTimeMillis();
            }
        } else {
            proxyList.add(stepPoint);

            //Store all of step points on list.
            forceOfStoreStepPoints();

            //Update user's data change.
            currentUser.steps_count = PrefManager.getInstance().getUserStepCount(currentUser.social_id);
            UserManager.getInstance().updateUser(currentUser);

            //Set last insert time.
            mLastInsertTime = System.currentTimeMillis();
        }
    }

    /**
     * Store points immediately
     */
    public void forceOfStoreStepPoints() {
        if (!proxyList.isEmpty()) {
            //Store all of step points on list.
            StepManager.getInstance().setStepPoints(proxyList);

            //Clear proxy.
            proxyList = new ArrayList<>();
        }
    }
}
