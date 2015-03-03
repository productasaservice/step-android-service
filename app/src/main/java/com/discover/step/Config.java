package com.discover.step;

import java.text.SimpleDateFormat;

/**
 * Created by Morpheus on 2015.01.04..
 */
public class Config {

    public static final boolean IS_DEVELOPER_MODE = true;

    public static final String DB_NAME = "step_discover_db";
    public static final int DB_VERSION = 2;
    public static final int SPLASH_TIME_OUT = 1000; //1 second.
    public static final long UPDATE_SERVER_DATA_SYNC_ALARM_TRIGGER_AT_MILLIS = 120000;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy. MMMM dd.");
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    //Constants
    public static final String CONST_CHALLENGE_HAS_ENDED = "CONST_CHALLENGE_HAS_ENDED";
}
