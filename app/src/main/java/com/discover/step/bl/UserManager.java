package com.discover.step.bl;

import com.discover.step.bc.DatabaseConnector;
import com.discover.step.ex.DefaultStepException;
import com.discover.step.model.User;

/**
 * Created by Geri on 2015.01.24..
 */
public class UserManager {

    private static UserManager mInstance = null;

    public static UserManager getInstance() {
        if (mInstance == null) {
            mInstance = new UserManager();
        }
        return mInstance;
    }

    public User getUserBySocialId(String id) {
        try {
            return DatabaseConnector.getInstance().getUserBySocialId(id);
        } catch (DefaultStepException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getAuthenticatedUser() {
        try {
            return DatabaseConnector.getInstance().getLoggedInUser();
        } catch (DefaultStepException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void doLogin(User user) {
        try {
            user.isLoggedIn = true;

            if (getUserBySocialId(user.social_id) != null) {
                DatabaseConnector.getInstance().updateUser(user);
            } else {
                DatabaseConnector.getInstance().setUser(user);
            }
        } catch (DefaultStepException e) {
            e.printStackTrace();
        }
    }

    public void doLogout(User user) {
        try {
            user.isLoggedIn = false;

            if (getUserBySocialId(user.social_id) != null) {
                DatabaseConnector.getInstance().updateUser(user);
            } else {
                DatabaseConnector.getInstance().setUser(user);
            }
        } catch (DefaultStepException e) {
            e.printStackTrace();
        }
    }
}
