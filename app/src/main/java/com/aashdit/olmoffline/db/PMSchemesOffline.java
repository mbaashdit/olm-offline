package com.aashdit.olmoffline.db;

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 31/07/21
 */
public class PMSchemesOffline extends RealmObject {
    @PrimaryKey
    public Long schemeId;
    public String schemeCode;
    public String schemeNameEn;
    public String schemeNameHi;
    public boolean isActive;
    public RealmList<PMActivitiesOffline> activityRealmList;

    public PMSchemesOffline() {
    }

    public static PMSchemesOffline parseSchemes(JSONObject object) {
        PMSchemesOffline oScheme = new PMSchemesOffline();
        oScheme.schemeId = object.optLong("schemeId");
        oScheme.schemeCode = object.optString("schemeCode");
        oScheme.schemeNameEn = object.optString("schemeNameEn");
        oScheme.schemeNameHi = object.optString("schemeNameHi");
        oScheme.isActive = object.optBoolean("isActive");
        oScheme.activityRealmList = new RealmList<>();

        PMActivitiesOffline activity = new PMActivitiesOffline();
        activity.activityCode="ACTIVITY";
        activity.activityId=0L;
        activity.activityNameEn="Select Activity";
        activity.activityNameHi="Select Activity";

        JSONArray activityJArray = object.optJSONArray("activity");
        if (activityJArray != null && activityJArray.length() > 0) {
            oScheme.activityRealmList.add(activity);
            for (int i = 0; i < activityJArray.length(); i++) {
                oScheme.activityRealmList.add(PMActivitiesOffline.parseActivity(activityJArray.optJSONObject(i)));
            }
        }

        return oScheme;
    }

    @Override
    public String toString() {
        return schemeNameEn;
    }
}
