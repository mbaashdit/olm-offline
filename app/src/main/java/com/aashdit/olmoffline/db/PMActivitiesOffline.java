package com.aashdit.olmoffline.db;

import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 31/07/21
 */
public class PMActivitiesOffline extends RealmObject {
    @PrimaryKey
    public Long activityId;
    public String activityCode;
    public String activityNameHi;
    public String activityNameEn;
//    public RealmList<String> schemesList = new RealmList<>();


    public static PMActivitiesOffline parseActivity(JSONObject object){
        PMActivitiesOffline activity = new PMActivitiesOffline();
        activity.activityId = object.optLong("activityId");
        activity.activityCode = object.optString("activityCode");
        activity.activityNameHi = object.optString("activityNameHi");
        activity.activityNameEn = object.optString("activityNameEn");
//        activity.schemesList = new RealmList<>();
//        activity.schemesList.add(schemeId.toString());
        return activity;
    }

    @Override
    public String toString() {
        return activityNameEn;
    }
}
