package com.discover.step.model;

import android.graphics.Color;

import com.discover.step.bl.UserManager;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by Geri on 2015.03.02..
 */
public class Challenge implements Serializable{
    public long id;
    public String title;
    public String challange_id;
    public String owner_id;
    public String color;
    public double lat = -1;
    public double lng = -1;
    public String opoment_one_id;
    public String opoment_two_id;
    public String opoment_three_id;
    public long duration;
    public int type; // 0 - catch me, 1 - catch place
    public String message;
    public String bet;

    public Challenge() {
        challange_id = generateId();
        owner_id = UserManager.getInstance().getAuthenticatedUser().social_id;
        color = String.format("#%06X", 0xFFFFFF & generateColor());
    }

    public Challenge(ParseObject object){
        challange_id = object.getString("challange_id");
        owner_id = object.getString("owner_id");
        color = object.getString("color");
        lat = Long.parseLong(object.getString("lat"));
        lng = Long.parseLong(object.getString("lng"));
        opoment_one_id = object.getString("opoment_one_id");
        opoment_two_id = object.getString("opoment_two_id");
        opoment_three_id = object.getString("opoment_three_id");
        duration = object.getLong("duration");
        type = object.getInt("type");
        message = object.getString("title");
        message = object.getString("message");
        bet = object.getString("bet");
    }

    public Challenge toChallange(ParseObject object) {
        challange_id = object.getString("challange_id");
        owner_id = object.getString("owner_id");
        color = object.getString("color");
        lat = Long.parseLong(object.getString("lat"));
        lng = Long.parseLong(object.getString("lng"));
        opoment_one_id = object.getString("opoment_one_id");
        opoment_two_id = object.getString("opoment_two_id");
        opoment_three_id = object.getString("opoment_three_id");
        duration = object.getLong("duration");
        type = object.getInt("type");
        message = object.getString("title");
        message = object.getString("message");
        bet = object.getString("bet");
        return this;
    }

    public ParseObject toParseObject() {
        ParseObject request = new ParseObject("Challange");
        request.put("challange_id",challange_id);
        request.put("owner_id",owner_id);
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
