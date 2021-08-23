package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class Frequency {
    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;

    public static Frequency parseFrequency(JSONObject object){
        Frequency shgType = new Frequency();
        shgType.valueId = object.optLong("valueId");
        shgType.valueCode = object.optString("valueCode");
        shgType.valueEn = object.optString("valueEn");
        shgType.valueHi = object.optString("valueHi");
        shgType.code = object.optString("code");
        shgType.isActive = object.optBoolean("isActive");
        return shgType;
    }
}
