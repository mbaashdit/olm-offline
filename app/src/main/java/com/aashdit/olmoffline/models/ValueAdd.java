package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class ValueAdd {
    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;
    public boolean isSelected;

    public static ValueAdd parseValueAddData(JSONObject object) {
        ValueAdd valueAdd = new ValueAdd();
        valueAdd.valueId = object.optLong("valueId");
        valueAdd.valueCode = object.optString("valueCode");
        valueAdd.valueEn = object.optString("valueEn");
        valueAdd.valueHi = object.optString("valueHi");
        valueAdd.code = object.optString("code");
        valueAdd.isActive = object.optBoolean("isActive");
        return valueAdd;
    }
}
