package com.discover.step.model;

import android.location.Location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Geri on 2015.01.18..
 */
@DatabaseTable
public class StepPoint {
    @DatabaseField(generatedId = true)
    public long id;
    @DatabaseField
    public long user_id;
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

    public void bindLocation(Location location) {
        created_at = System.currentTimeMillis();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
        bearing = location.getBearing();
        speed = location.getSpeed();
    }
}
