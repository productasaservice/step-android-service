package com.discover.step.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseObject;

/**
 * Created by Geri on 2015.01.25..
 */
@DatabaseTable
public class Badge {
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField
    public String achievement_id;
    @DatabaseField
    public String user_social_id;
    @DatabaseField
    public long created_at;

    public Badge() {}
    public Badge(ParseObject object) {
        achievement_id = object.getString("achievement_id");
        user_social_id = object.getString("user_social_id");
        created_at = object.getLong("created_at");
    }

    public ParseObject toParseObject() {
        ParseObject object = new ParseObject("UsersBadges");
        object.put("achievement_id",achievement_id);
        object.put("user_social_id",user_social_id);
        object.put("created_at",created_at);

        return object;
    }

    public Badge toBadge(ParseObject object) {
        achievement_id = object.getString("achievement_id");
        user_social_id = object.getString("user_social_id");
        created_at = object.getLong("created_at");

        return this;
    }
}
