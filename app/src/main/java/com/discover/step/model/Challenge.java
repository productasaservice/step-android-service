package com.discover.step.model;

import com.parse.ParseObject;

/**
 * Created by Geri on 2015.03.02..
 */
public class Challenge {
    public long id;
    public String challange_id;
    public String owner_id;
    public String opoment_one_id;
    public String opoment_two_id;
    public String opoment_three_id;
    public long duration;
    public int type; // 0 - catch me, 1 - catch place
    public String message;
    public String bet;

    public Challenge() {}
    public Challenge(ParseObject object){
        challange_id = object.getString("challange_id");
        owner_id = object.getString("owner_id");
        opoment_one_id = object.getString("opoment_one_id");
        opoment_two_id = object.getString("opoment_two_id");
        opoment_three_id = object.getString("opoment_three_id");
        duration = object.getLong("duration");
        type = object.getInt("type");
        message = object.getString("message");
        bet = object.getString("bet");
    }

    public Challenge toChallange(ParseObject object) {
        challange_id = object.getString("challange_id");
        owner_id = object.getString("owner_id");
        opoment_one_id = object.getString("opoment_one_id");
        opoment_two_id = object.getString("opoment_two_id");
        opoment_three_id = object.getString("opoment_three_id");
        duration = object.getLong("duration");
        type = object.getInt("type");
        message = object.getString("message");
        bet = object.getString("bet");
        return this;
    }

    public ParseObject toParseObject() {
        ParseObject request = new ParseObject("Challange");
        request.put("challange_id",challange_id);
        request.put("owner_id",owner_id);
        request.put("opoment_one_id",opoment_one_id == null ? "empty" : opoment_one_id);
        request.put("opoment_one_id",opoment_two_id == null ? "empty" : opoment_two_id);
        request.put("opoment_one_id",opoment_three_id == null ? "empty" : opoment_three_id);
        request.put("duration",duration);
        request.put("type",type);
        request.put("message",message);
        request.put("bet",bet);

        return request;
    }
}
