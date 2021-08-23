package com.aashdit.olmoffline.models;

import org.json.JSONObject;

/**
 * Created by Manabendu on 27/03/21
 */
public class SeedType {
    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;
    public boolean isSelected;

    public static SeedType parseSeedType(JSONObject object) {
        SeedType socialCategory = new SeedType();
        socialCategory.valueId = object.optLong("valueId");
        socialCategory.valueCode = object.optString("valueCode");
        socialCategory.valueEn = object.optString("valueEn");
        socialCategory.valueHi = object.optString("valueHi");
        socialCategory.code = object.optString("code");
        socialCategory.isActive = object.optBoolean("isActive");
        return socialCategory;
    }
}
