package com.aashdit.olmoffline.models;

import com.aashdit.olmoffline.db.shg.ShgList;

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Activity extends RealmObject {

    @PrimaryKey
    private Long activityId;
    private String activityCode;
    private String activityNameHi;
    private String activityNameEn;
//    public RealmList<ShgList> shgList;
//    private RealmList<String> schemesList = new RealmList<>();

//    static public Long schemeId;

    public Activity() {
    }

//    public RealmList<String> getSchemesList() {
//        return schemesList;
//    }

//    public void setSchemesList(RealmList<String> schemesList) {
//        this.schemesList = schemesList;
//    }

    public static Activity parseActivity(JSONObject object,Long schemeId,Long activityTypeId){
        Activity activity = new Activity();
        activity.activityId = object.optLong("activityId");
        activity.activityCode = object.optString("activityCode");
        activity.activityNameHi = object.optString("activityNameHi");
        activity.activityNameEn = object.optString("activityNameEn");
//        activity.shgList = new RealmList<>();
        JSONArray shgArray = object.optJSONArray("shgList");

        if (shgArray != null && shgArray.length()>0){
            for (int i = 0; i < shgArray.length() ; i++) {
//                activity.shgList.add(ShgList.parseShgList(shgArray.optJSONObject(i),schemeId,activityTypeId,activity.activityId));
            }
        }

//        activity.schemesList = new RealmList<>();
//        activity.schemesList.add(schemeId.toString());
        return activity;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getActivityCode() {
        return activityCode;
    }

    public void setActivityCode(String activityCode) {
        this.activityCode = activityCode;
    }

    public String getActivityNameHi() {
        return activityNameHi;
    }

    public void setActivityNameHi(String activityNameHi) {
        this.activityNameHi = activityNameHi;
    }

    public String getActivityNameEn() {
        return activityNameEn;
    }

    public void setActivityNameEn(String activityNameEn) {
        this.activityNameEn = activityNameEn;
    }

    @Override
    public String toString() {
        return  activityNameEn ;
    }
}
