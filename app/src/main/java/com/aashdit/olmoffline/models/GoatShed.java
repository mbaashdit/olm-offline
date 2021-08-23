package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class GoatShed {
    private Long valueId;
    private String valueCode;
    private String code;
    private String valueEn;
    private String valueHi;
    private  boolean isActive;

    public static GoatShed parseGoatShed(JSONObject object){
        GoatShed goatShed = new GoatShed();
        goatShed.valueId = object.optLong("valueId");
        goatShed.valueCode = object.optString("valueCode");
        goatShed.valueEn = object.optString("valueEn");
        goatShed.valueHi = object.optString("valueHi");
        goatShed.isActive = object.optBoolean("isActive");
        return goatShed;
    }


    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public String getValueCode() {
        return valueCode;
    }

    public void setValueCode(String valueCode) {
        this.valueCode = valueCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValueEn() {
        return valueEn;
    }

    public void setValueEn(String valueEn) {
        this.valueEn = valueEn;
    }

    public String getValueHi() {
        return valueHi;
    }

    public void setValueHi(String valueHi) {
        this.valueHi = valueHi;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
