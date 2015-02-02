package com.discover.step.util;

import com.discover.step.model.Day;
import com.discover.step.model.StepPoint;
import com.discover.step.model.User;
import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

/**
 * Created by Geri on 2015.01.18..
 */
public class DataBaseConfigUtil extends OrmLiteConfigUtil {

    private static final Class<?>[] classes = new Class[] {StepPoint.class, User.class, Day.class
    };

    /**
     * Database configuration file helper class that is used to write a configuration file into the raw resource sub-directory to speed up DAO creation.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        writeConfigFile("ormlite_config.txt", classes);
    }
}
