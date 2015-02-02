package com.discover.step.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseObject;

/**
 * Created by Geri on 2015.01.25..
 */
@DatabaseTable
public class Achievement {
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField
    public String achievement_id;
    @DatabaseField
    public String name;
    @DatabaseField
    public String description;
    @DatabaseField
    public String message;
    @DatabaseField
    public int goal;
    @DatabaseField
    public int type; // 0 - steppoint,...

    public Achievement() {}
    public Achievement(ParseObject object)  {
        achievement_id = object.getString("achievement_id");
        name = object.getString("name");
        description = object.getString("description");
        message = object.getString("message");
        goal = object.getInt("goal");
        type = object.getInt("type");
    }

    public Achievement toAchievement(ParseObject object) {
        achievement_id = object.getString("achievement_id");
        name = object.getString("name");
        description = object.getString("description");
        message = object.getString("message");
        goal = object.getInt("goal");
        type = object.getInt("type");

        return this;
    }
}
