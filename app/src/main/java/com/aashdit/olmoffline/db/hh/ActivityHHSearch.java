package com.aashdit.olmoffline.db.hh;

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Manabendu on 06/08/21
 */
public class ActivityHHSearch extends RealmObject {

    public Long activityId;
    public Long schemeId;
    public String activityCode;
    public String activityNameHi;
    public String activityNameEn;
    public RealmList<HHSHGList> shgList;
//    private RealmList<String> schemesList = new RealmList<>();

//    static public Long schemeId;

    public ActivityHHSearch() {
    }

//    public RealmList<String> getSchemesList() {
//        return schemesList;
//    }

//    public void setSchemesList(RealmList<String> schemesList) {
//        this.schemesList = schemesList;
//    }

    public static ActivityHHSearch parseActivitySearch(JSONObject object,Long _schemeId,Long _activityId){
        ActivityHHSearch activity = new ActivityHHSearch();
        activity.activityId = object.optLong("activityId");
        activity.activityCode = object.optString("activityCode");
        activity.activityNameHi = object.optString("activityNameHi");
        activity.activityNameEn = object.optString("activityNameEn");
        activity.schemeId = _schemeId;
        activity.shgList = new RealmList<>();
        JSONArray shgArray = object.optJSONArray("shgList");

        if (shgArray != null && shgArray.length()>0){
            for (int i = 0; i < shgArray.length() ; i++) {
                activity.shgList.add(HHSHGList.parseShgList(shgArray.optJSONObject(i),_schemeId,activity.activityId));
            }
        }

//        activity.schemesList = new RealmList<>();
//        activity.schemesList.add(schemeId.toString());
        return activity;
    }

}
