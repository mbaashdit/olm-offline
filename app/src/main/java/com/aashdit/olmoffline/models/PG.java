package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class PG {
    public Long entityId;
    public String entityName;

    public static PG parsePG(JSONObject object){
        PG villageId = new PG();
        villageId.entityId = object.optLong("entityId");
        villageId.entityName = object.optString("entityName");
        return villageId;
    }
    @Override
    public String toString() {
        return entityName;
    }
}
