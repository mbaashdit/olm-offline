package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class Disability {

    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;

    public static Disability parseDisability(JSONObject object) {
        Disability disability = new Disability();
        disability.valueId = object.optLong("valueId");
        disability.valueCode = object.optString("valueCode");
        disability.valueEn = object.optString("valueEn");
        disability.valueHi = object.optString("valueHi");
        disability.code = object.optString("code");
        disability.isActive = object.optBoolean("isActive");
        return disability;
    }
}
