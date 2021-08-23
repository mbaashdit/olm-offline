package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class EG {
    public Long entityId;
    public String entityName;

    public static EG parseEG(JSONObject object){
        EG villageId = new EG();
        villageId.entityId = object.optLong("entityId");
        villageId.entityName = object.optString("entityName");
        return villageId;
    }
    @Override
    public String toString() {
        return entityName;
    }
}
