package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class Gender {
    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;

    public static Gender parseGender(JSONObject object) {
        Gender gender = new Gender();
        gender.valueId = object.optLong("valueId");
        gender.valueCode = object.optString("valueCode");
        gender.valueEn = object.optString("valueEn");
        gender.valueHi = object.optString("valueHi");
        gender.code = object.optString("code");
        gender.isActive = object.optBoolean("isActive");
        return gender;
    }
}
