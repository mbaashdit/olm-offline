package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class Cluster {

    public Long entityId;
    public String entityName;

    public static Cluster parseClusterData(JSONObject object) {
        Cluster cluster = new Cluster();
        cluster.entityId = object.optLong("entityId");
        cluster.entityName = object.optString("entityName");
        return cluster;
    }
}
