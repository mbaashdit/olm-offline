package com.aashdit.olmoffline.db.hh;

import com.aashdit.olmoffline.models.ActivityType;
import com.aashdit.olmoffline.utils.Constant;

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Manabendu on 06/08/21
 */
public class SchemeHHSearch extends RealmObject {
    public Long schemeId;
    public String schemeCode;
    public String schemeNameEn;
    public String schemeNameHi;
    public boolean isActive;
    public RealmList<ActivityHHSearch> activityRealmList;
    public RealmList<ActivityType> activityTypeRealmList;

    public SchemeHHSearch() {
    }

    public static SchemeHHSearch parseSchemeSearch(JSONObject object,String userType,String dataType) {
        SchemeHHSearch schemas = new SchemeHHSearch();
        schemas.schemeId = object.optLong("schemeId");
        schemas.schemeCode = object.optString("schemeCode");
        schemas.schemeNameEn = object.optString("schemeNameEn");
        schemas.schemeNameHi = object.optString("schemeNameHi");
        schemas.isActive = object.optBoolean("isActive");

        if (userType.equals(Constant.PMITRA)) {
            schemas.activityRealmList = new RealmList<>();
            ActivityHHSearch activity = new ActivityHHSearch();
        activity.activityCode="ACTIVITY";
        activity.activityId=0L;
        activity.activityNameEn="Select Activity";
        activity.activityNameHi="Select Activity";

            JSONArray activityJArray = object.optJSONArray("activity");
            if (activityJArray != null && activityJArray.length() > 0) {
            schemas.activityRealmList.add(activity);
                for (int i = 0; i < activityJArray.length(); i++) {
                    schemas.activityRealmList.add(ActivityHHSearch.parseActivitySearch(activityJArray.optJSONObject(i), schemas.schemeId,0L));
                }
            }
        }else {
            schemas.activityTypeRealmList = new RealmList<>();

            ActivityType activityType = new ActivityType();
            activityType.setActivityCode("ActivityType");
            activityType.setActivityId(0L);
            activityType.setActivityTypeDesc("Select ActivityType");
            activityType.setActive(true);

            JSONArray actTypeArray = object.optJSONArray("activityType");
            if (actTypeArray != null && actTypeArray.length() > 0) {
                schemas.activityTypeRealmList.add(activityType);
                for (int i = 0; i < actTypeArray.length(); i++) {
                    schemas.activityTypeRealmList.add(ActivityType.parseActivityType(actTypeArray.optJSONObject(i),schemas.schemeId,userType,dataType));
                }
            }
        }
        return schemas;
    }

}
