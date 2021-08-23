package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class SocialCategory {
    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;

    public static SocialCategory parseSocialCategory(JSONObject object) {
        SocialCategory socialCategory = new SocialCategory();
        socialCategory.valueId = object.optLong("valueId");
        socialCategory.valueCode = object.optString("valueCode");
        socialCategory.valueEn = object.optString("valueEn");
        socialCategory.valueHi = object.optString("valueHi");
        socialCategory.code = object.optString("code");
        socialCategory.isActive = object.optBoolean("isActive");
        return socialCategory;
    }
}
