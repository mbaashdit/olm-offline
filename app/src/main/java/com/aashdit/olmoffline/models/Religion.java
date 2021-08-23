package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class Religion {
    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;

    public static Religion parseReligion(JSONObject object) {
        Religion religion = new Religion();
        religion.valueId = object.optLong("valueId");
        religion.valueCode = object.optString("valueCode");
        religion.valueEn = object.optString("valueEn");
        religion.valueHi = object.optString("valueHi");
        religion.code = object.optString("code");
        religion.isActive = object.optBoolean("isActive");
        return religion;
    }
}
