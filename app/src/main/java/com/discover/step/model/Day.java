package com.discover.step.model;

import android.util.Log;

import com.discover.step.Config;
import com.discover.step.Session;
import com.discover.step.bl.UserManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Geri on 2015.01.31..
 */
@DatabaseTable
public class Day {
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField
    public String day_id;
    @DatabaseField
    public String social_id;
    @DatabaseField
    public String date;
    @DatabaseField
    public long date_ts;
    @DatabaseField
    public boolean isSynced = true;

    public Day(){}
    public Day(ParseObject object) {
        try {
            day_id = object.getString("day_id");
            date = object.getString("date");
            social_id = object.getString("user_social_id");
            date_ts = Config.DATE_FORMAT.parse(date).getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void create() {
        if (Session.authenticated_user_social_id == null)
            Session.start();

        try {
        long now = System.currentTimeMillis();
            social_id = Session.authenticated_user_social_id;
            date = Config.DATE_FORMAT.format(new Date(now));
            date_ts = Config.DATE_FORMAT.parse(date).getTime();
            day_id = social_id + "_" + date_ts;
            isSynced = false;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public ParseObject toParseObject() {
        ParseObject object = new ParseObject("Date");
        object.put("day_id",day_id);
        object.put("date",date);
        object.put("user_social_id",social_id);

        return object;
    }

    public Day toDay(ParseObject object){
        try {
            day_id = object.getString("day_id");
            date = object.getString("date");
            social_id = object.getString("social_id");
            date_ts = Config.DATE_FORMAT.parse(date).getTime();

            return this;
        } catch (ParseException e) {
            e.printStackTrace();
            return this;
        }
    }
}
