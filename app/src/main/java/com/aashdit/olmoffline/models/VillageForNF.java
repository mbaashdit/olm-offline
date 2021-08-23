package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class VillageForNF {
    public Long entityId;
    public String entityName;

    public static VillageForNF parseVillage(JSONObject object){
        VillageForNF villageId = new VillageForNF();
        villageId.entityId = object.optLong("entityId");
        villageId.entityName = object.optString("entityName");
        return villageId;
    }

    @Override
    public String toString() {
        return entityName;
    }
}
