package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class Village /*extends RealmObject*/ {
    public String villageName;
    public String villageCode;
    public String extraProps;
    public Long village_id;

    public static Village parseVillage(JSONObject object){
        Village village = new Village();
        village.villageCode = object.optString("villageCode");
        village.villageName = object.optString("villageName");
        village.extraProps = object.optString("extraProps");
        village.village_id = object.optLong("villageId");
        return village;
    }
}
