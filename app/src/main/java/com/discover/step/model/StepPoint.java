package com.discover.step.model;

import android.location.Location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseObject;

/**
 * Created by Geri on 2015.01.18..
 */
@DatabaseTable
public class StepPoint {
    @DatabaseField(generatedId = true)
    public long id;
    @DatabaseField
    public String user_social_id;
    @DatabaseField
    public long created_at;
    @DatabaseField
    public double latitude;
    @DatabaseField
    public double longitude;
    @DatabaseField
    public float accuracy;
    @DatabaseField
    public float bearing;
    @DatabaseField
    public float speed;
    @DatabaseField
    public String color;
    @DatabaseField
    public String description;
    @DatabaseField
    public boolean isDrawnPoint = false;
    @DatabaseField
    public boolean isVisibleOnMap = false;

    public void bindLocation(Location location) {
        created_at = System.currentTimeMillis();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
        bearing = location.getBearing();
        speed = location.getSpeed();
    }

    public ParseObject toParseObject() {
        ParseObject object = new ParseObject("StepPoint");
        object.put("user_social_id",user_social_id);
        object.put("latitude",latitude);
        object.put("longitude",longitude);
        object.put("created_at",created_at);
        object.put("accuracy",accuracy);
        object.put("bearing",bearing);
        object.put("speed",speed);
        object.put("color",color);
        object.put("description",description);
        object.put("is_drawn_point",isDrawnPoint);

        return object;
    }

    public StepPoint toStepPoint(ParseObject object) {
        user_social_id = object.getString("user_social_id");
        latitude = Double.parseDouble(object.getString("latitude"));
        latitude = Double.parseDouble(object.getString("longitude"));
        created_at = object.getLong("created_at");
        color = object.getString("color");
        description = object.getString("description");
        isDrawnPoint = object.getBoolean("is_drawn_point");
        return this;
    }
}
