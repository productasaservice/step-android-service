package com.discover.step.model;

import android.graphics.Color;

import com.discover.step.bl.UserManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by Geri on 2015.03.02..
 */
@DatabaseTable
public class Challenge implements Serializable{
    @DatabaseField(generatedId = true)
    public long id;
    @DatabaseField
    public String title;
    @DatabaseField
    public String challange_id;
    @DatabaseField
    public String owner_id;
    @DatabaseField
    public String winner_id;
    @DatabaseField
    public String color;
    @DatabaseField
    public double lat = -1;
    @DatabaseField
    public double lng = -1;
    @DatabaseField
    public String opoment_one_id;
    @DatabaseField
    public String opoment_two_id;
    @DatabaseField
    public String opoment_three_id;
    @DatabaseField
    public long duration;
    @DatabaseField
    public int type; // 0 - catch me, 1 - catch place
    @DatabaseField
    public String message;
    @DatabaseField
    public String bet;
    @DatabaseField
    public boolean isChallengeRequestNoticed = false;
    @DatabaseField
    public boolean isChallengeOver = false;
    @DatabaseField
    public boolean isDeleted = true;


    public Challenge() {
        challange_id = generateId();
        owner_id = UserManager.getInstance().getAuthenticatedUser().social_id;
        color = String.format("#%06X", 0xFFFFFF & generateColor());
    }

    public Challenge(ParseObject object){
        challange_id = object.getString("challange_id");
        owner_id = object.getString("owner_id");
        winner_id = object.getString("winner_id");
        color = object.getString("color");
        lat = Double.parseDouble(object.getString("lat"));
        lng = Double.parseDouble(object.getString("lng"));
        opoment_one_id = object.getString("opoment_one_id");
        opoment_two_id = object.getString("opoment_two_id");
        opoment_three_id = object.getString("opoment_three_id");
        duration = object.getLong("duration");
        type = object.getInt("type");
        title = object.getString("title");
        message = object.getString("message");
        bet = object.getString("bet");
        isChallengeOver = winner_id.endsWith("empty") ? false : true;
    }

    public Challenge toChallange(ParseObject object) {
        challange_id = object.getString("challange_id");
        owner_id = object.getString("owner_id");
        winner_id = object.getString("winner_id");
        color = object.getString("color");
        lat = Long.parseLong(object.getString("lat"));
        lng = Long.parseLong(object.getString("lng"));
        opoment_one_id = object.getString("opoment_one_id");
        opoment_two_id = object.getString("opoment_two_id");
        opoment_three_id = object.getString("opoment_three_id");
        duration = object.getLong("duration");
        type = object.getInt("type");
        title = object.getString("title");
        message = object.getString("message");
        bet = object.getString("bet");
        isChallengeOver = winner_id.endsWith("empty") ? false : true;

        return this;
    }

    public ParseObject toParseObject() {
        ParseObject request = new ParseObject("Challange");
        request.put("challange_id",challange_id);
        request.put("owner_id",owner_id);
        request.put("winner_id",winner_id == null ? "empty" : winner_id);
        request.put("color",color);
        request.put("lat",lat + "");
        request.put("lng",lng + "");
        request.put("opoment_one_id",opoment_one_id == null ? "empty" : opoment_one_id);
        request.put("opoment_two_id",opoment_two_id == null ? "empty" : opoment_two_id);
        request.put("opoment_three_id",opoment_three_id == null ? "empty" : opoment_three_id);
        request.put("duration",duration);
        request.put("type",type);
        request.put("title",title);
        request.put("message",message);
        request.put("bet",bet);

        return request;
    }

    private String generateId() {
        char[] chars = "123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;
    }

    private int generateColor() {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        return color;
    }
}
