package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class PipCategory {
    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;

    public static PipCategory parsePipCategory(JSONObject object) {
        PipCategory pipCategory = new PipCategory();
        pipCategory.valueId = object.optLong("valueId");
        pipCategory.valueCode = object.optString("valueCode");
        pipCategory.valueEn = object.optString("valueEn");
        pipCategory.valueHi = object.optString("valueHi");
        pipCategory.code = object.optString("code");
        pipCategory.isActive = object.optBoolean("isActive");
        return pipCategory;
    }
}
