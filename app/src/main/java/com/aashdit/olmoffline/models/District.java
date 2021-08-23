package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class District /*extends RealmObject*/ {
//    @PrimaryKey
    public Long districtId;
    public String districtCode;
    public String districtNameEN;
    public String districtNameHI;
    public String districtName;
    public boolean isActive;

    public static District parseDistrict(JSONObject object){
        District district = new District();
        district.districtCode = object.optString("districtCode");
        district.districtNameEN = object.optString("districtNameEN");
        district.districtNameHI = object.optString("districtNameHI");
        district.districtName = object.optString("districtName");
        district.districtId = object.optLong("districtId");
        district.isActive = object.optBoolean("isActive");
        return district;
    }
}
