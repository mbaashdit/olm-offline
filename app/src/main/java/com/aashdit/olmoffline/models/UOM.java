package com.aashdit.olmoffline.models;

import org.json.JSONObject;

/**
 * Created by Manabendu on 28/03/21
 */
public class UOM {
    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;

    public static UOM parseUOM(JSONObject object) {
        UOM socialCategory = new UOM();
        socialCategory.valueId = object.optLong("valueId");
        socialCategory.valueCode = object.optString("valueCode");
        socialCategory.valueEn = object.optString("valueEn");
        socialCategory.valueHi = object.optString("valueHi");
        socialCategory.code = object.optString("code");
        socialCategory.isActive = object.optBoolean("isActive");
        return socialCategory;
    }
}
