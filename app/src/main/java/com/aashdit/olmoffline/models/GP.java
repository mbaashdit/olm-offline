package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class GP /*extends RealmObject*/ {
    public long gpId;
    public String gpCode;
    public String gpName;

    public static GP parseGp(JSONObject object){
        GP gp = new GP();
        gp.gpCode = object.optString("gpCode");
        gp.gpName = object.optString("gpName");
        gp.gpId = object.optLong("gpId");
        return gp;
    }
}
