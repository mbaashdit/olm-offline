package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class MeetingFrequency {
    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;

    public static MeetingFrequency parseMeetingFrequency(JSONObject object){
        MeetingFrequency mf = new MeetingFrequency();
        mf.valueId = object.optLong("valueId");
        mf.valueCode = object.optString("valueCode");
        mf.valueEn = object.optString("valueEn");
        mf.valueHi = object.optString("valueHi");
        mf.code = object.optString("code");
        mf.isActive = object.optBoolean("isActive");
        return mf;
    }
}
