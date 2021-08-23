package com.aashdit.olmoffline.db.shg;

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Manabendu on 05/08/21
 */
public class ActivitySHGSearch extends RealmObject {

    public Long activityId;
    public String activityCode;
    public String activityNameHi;
    public String activityNameEn;
    public RealmList<ShgList> shgList;
//    private RealmList<String> schemesList = new RealmList<>();

//    static public Long schemeId;

    public ActivitySHGSearch() {
    }

//    public RealmList<String> getSchemesList() {
//        return schemesList;
//    }

//    public void setSchemesList(RealmList<String> schemesList) {
//        this.schemesList = schemesList;
//    }

    public static ActivitySHGSearch parseActivitySearch(JSONObject object,Long schemeId,Long activityTypeId){
        ActivitySHGSearch activity = new ActivitySHGSearch();
        activity.activityId = object.optLong("activityId");
        activity.activityCode = object.optString("activityCode");
        activity.activityNameHi = object.optString("activityNameHi");
        activity.activityNameEn = object.optString("activityNameEn");
        activity.shgList = new RealmList<>();
        JSONArray shgArray = object.optJSONArray("shgList");

        if (shgArray != null && shgArray.length()>0){
            for (int i = 0; i < shgArray.length() ; i++) {
                activity.shgList.add(ShgList.parseShgList(shgArray.optJSONObject(i),schemeId,activityTypeId,activity.activityId));
            }
        }

//        activity.schemesList = new RealmList<>();
//        activity.schemesList.add(schemeId.toString());
        return activity;
    }

}
