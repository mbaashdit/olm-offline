package com.aashdit.olmoffline.db.shg;

import com.aashdit.olmoffline.models.Activity;
import com.aashdit.olmoffline.models.ActivityType;
import com.aashdit.olmoffline.utils.Constant;

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Manabendu on 05/08/21
 */
public class SchemeSHGSearch extends RealmObject {
    public Long schemeId;
    public String schemeCode;
    public String schemeNameEn;
    public String schemeNameHi;
    public boolean isActive;
    public RealmList<ActivitySHGSearch> activityRealmList;
    public RealmList<ActivityType> activityTypeRealmList;


    public SchemeSHGSearch() {
    }

    public static SchemeSHGSearch parseSchemeSearch(JSONObject object, String userType,String dataType) {
        SchemeSHGSearch schemas = new SchemeSHGSearch();
        schemas.schemeId = object.optLong("schemeId");
        schemas.schemeCode = object.optString("schemeCode");
        schemas.schemeNameEn = object.optString("schemeNameEn");
        schemas.schemeNameHi = object.optString("schemeNameHi");
        schemas.isActive = object.optBoolean("isActive");

        if (userType.equals(Constant.PMITRA)) {
            schemas.activityRealmList = new RealmList<>();
//            Activity activity = new Activity();
//            activity.setActivityCode("ACTIVITY");
//            activity.setActivityId(0L);
//            activity.setActivityNameEn("Select Activity");
//            activity.setActivityNameHi("Select Activity");

            JSONArray activityJArray = object.optJSONArray("activity");
            if (activityJArray != null && activityJArray.length() > 0) {
//                schemas.activityRealmList.add(activity);
                for (int i = 0; i < activityJArray.length(); i++) {
                    schemas.activityRealmList.add(ActivitySHGSearch.parseActivitySearch(activityJArray.optJSONObject(i),schemas.schemeId,0L));
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
