package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class HHEntity {
    public Long entityId;
    public String entityName;

    public static HHEntity parseHHEntity(JSONObject object) {
        HHEntity entity = new HHEntity();
        entity.entityId = object.optLong("entityId");
        entity.entityName = object.optString("entityName");
        return entity;
    }

    @Override
    public String toString() {
        return entityName;
    }
}
