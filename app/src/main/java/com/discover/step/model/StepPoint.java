package com.discover.step.model;

import android.content.Context;
import android.location.Location;

import com.discover.step.R;
import com.discover.step.StepApplication;
import com.discover.step.bl.PrefManager;
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
    @DatabaseField
    public boolean isSynced = true;

    public void bindLocation(Location location) {
        boolean is_highlighted_enabled = PrefManager.getInstance().getIsHighlightedEnabled();
        Context context = StepApplication.getContext();

        created_at = System.currentTimeMillis();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
        bearing = location.getBearing();
        speed = location.getSpeed();
        color = is_highlighted_enabled ?
                "#" + Integer.toHexString(context.getResources().getColor(R.color.main_marker_color)) :
                "#" + Integer.toHexString(context.getResources().getColor(R.color.secondary_marker_color));
        isDrawnPoint = is_highlighted_enabled;
    }

    public StepPoint() {

    }
    public StepPoint(ParseObject object) {
        user_social_id = object.getString("user_social_id");
        latitude = Double.parseDouble(object.getString("latitude"));
        longitude = Double.parseDouble(object.getString("longitude"));
        created_at = object.getLong("created_at");
        color = object.getString("color");
        description = object.getString("description");
        isDrawnPoint = object.getBoolean("is_drawn_point");
    }

    public ParseObject toParseObject() {
        ParseObject object = new ParseObject("StepPoint");
        if (user_social_id != null)
            object.put("user_social_id", user_social_id);

        if (latitude > -1)
            object.put("latitude", ""+latitude);

        if (longitude > -1)
            object.put("longitude", ""+longitude);

        if (created_at > -1)
            object.put("created_at", created_at);

        if (accuracy > -1)
            object.put("accuracy",accuracy);

        if (bearing > -1)
            object.put("bearing",bearing);

        if (speed > -1)
            object.put("speed",speed);

        if (color != null)
            object.put("color",""+color);

        if (description != null)
            object.put("description",description);

        object.put("is_drawn_point",isDrawnPoint);

        return object;
    }

    public StepPoint toStepPoint(ParseObject object) {
        user_social_id = object.getString("user_social_id");
        latitude = Double.parseDouble(object.getString("latitude"));
        longitude = Double.parseDouble(object.getString("longitude"));
        created_at = object.getLong("created_at");
        color = object.getString("color");
        description = object.getString("description");
        isDrawnPoint = object.getBoolean("is_drawn_point");
        return this;
    }
}
