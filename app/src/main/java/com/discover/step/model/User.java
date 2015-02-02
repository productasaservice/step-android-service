package com.discover.step.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseObject;

/**
 * Created by Geri on 2015.01.24..
 */
@DatabaseTable
public class User {

    public static final int FACEBOOK = 0, GOOGLE_PLUS = 1;

    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField
    public String social_id;
    @DatabaseField
    public String first_name;
    @DatabaseField
    public String last_name;
    @DatabaseField
    public String email;
    @DatabaseField
    public String picture_url;
    @DatabaseField
    public int login_type;                  // 0 - facebook, 1 - google plus.
    @DatabaseField
    public boolean isLoggedIn = false;
    @DatabaseField
    public int steps_count;

    public User toUser(ParseObject object) {
        social_id = object.getString("social_id");
        first_name = object.getString("first_name");
        last_name = object.getString("last_name");
        email = object.getString("email");
        picture_url = object.getString("picture_url");
        login_type = object.getInt("login_type");
        steps_count = object.getInt("step_count");
        return this;
    }

    public ParseObject toParseObject() {
        ParseObject request = new ParseObject("StepUser");
        request.put("social_id",social_id);
        request.put("first_name",first_name);
        request.put("last_name",last_name);
        request.put("email",email);
        request.put("picture_url",picture_url);
        request.put("login_type",login_type);
        request.put("step_count",steps_count);

        return request;
    }
}
