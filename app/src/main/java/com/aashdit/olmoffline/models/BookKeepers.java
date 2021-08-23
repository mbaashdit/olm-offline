package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class BookKeepers {
    public Long valueId;
    public String valueCode;
    public String code;
    public String valueEn;
    public String valueHi;
    public boolean isActive;
    
    public static BookKeepers parseBookKeepers(JSONObject object){
        BookKeepers shgType = new BookKeepers();
        shgType.valueId = object.optLong("valueId");
        shgType.valueCode = object.optString("valueCode");
        shgType.valueEn = object.optString("valueEn");
        shgType.valueHi = object.optString("valueHi");
        shgType.code = object.optString("code");
        shgType.isActive = object.optBoolean("isActive");
        return shgType;
    }
}
