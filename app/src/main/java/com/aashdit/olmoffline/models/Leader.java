package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class Leader {
    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;

    public static Leader parseLeader(JSONObject object){
        Leader leader = new Leader();
        leader.valueId = object.optLong("valueId");
        leader.valueCode = object.optString("valueCode");
        leader.valueEn = object.optString("valueEn");
        leader.valueHi = object.optString("valueHi");
        leader.code = object.optString("code");
        leader.isActive = object.optBoolean("isActive");
        return leader;
    }
}
