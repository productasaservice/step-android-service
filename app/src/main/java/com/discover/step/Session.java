package com.discover.step;

import com.discover.step.bl.PrefManager;
import com.discover.step.bl.UserManager;
import com.discover.step.model.User;

/**
 * Created by Geri on 2015.01.31..
 */
public class Session {
    public static String authenticated_user_social_id;
    public static int step_count;
    public static User user;

    public static boolean start() {
        user = UserManager.getInstance().getAuthenticatedUser();
        if (user != null) {
            authenticated_user_social_id = UserManager.getInstance().getAuthenticatedUser().social_id;
            step_count = user.steps_count;
            return true;
        }
        return false;
    }

    public static void clear() {
        authenticated_user_social_id = null;
        step_count = 0;
    }

    public static void increaseStep() {
        step_count ++;
        if (authenticated_user_social_id != null)
            PrefManager.getInstance().setUserStepCount(authenticated_user_social_id,step_count);
    }

    public static String getAuthenticatedUserSocialId() {
        if (user == null)
            start();

        return authenticated_user_social_id;
    }
}
