package com.aashdit.olmoffline.models;

import com.aashdit.olmoffline.utils.Constant;

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Schemes extends RealmObject {

    @PrimaryKey
    public Long schemeId;
    public String schemeCode;
    public String schemeNameEn;
    public String schemeNameHi;
    public boolean isActive;
    public RealmList<Activity> activityRealmList;
    public RealmList<ActivityType> activityTypeRealmList;


    public Schemes() {
    }

    public static Schemes parseSchemes(JSONObject object, String userType) {
        Schemes schemas = new Schemes();
        schemas.setSchemeId(object.optLong("schemeId"));
        schemas.setSchemeCode(object.optString("schemeCode"));
        schemas.setSchemeNameEn(object.optString("schemeNameEn"));
        schemas.setSchemeNameHi(object.optString("schemeNameHi"));
        schemas.setActive(object.optBoolean("isActive"));
        if (userType.equals(Constant.PMITRA)) {
            schemas.activityRealmList = new RealmList<>();
            Activity activity = new Activity();
            activity.setActivityCode("ACTIVITY");
            activity.setActivityId(0L);
            activity.setActivityNameEn("Select Activity");
            activity.setActivityNameHi("Select Activity");
            JSONArray activityJArray = object.optJSONArray("activity");
            if (activityJArray != null && activityJArray.length() > 0) {
                schemas.activityRealmList.add(activity);
                for (int i = 0; i < activityJArray.length(); i++) {
                    schemas.activityRealmList.add(Activity.parseActivity(activityJArray.optJSONObject(i),schemas.getSchemeId(),0L));
                }
            }
        } else {
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
                    schemas.activityTypeRealmList.add(ActivityType.parseActivityType(actTypeArray.optJSONObject(i),schemas.getSchemeId(),userType,""));
                }
            }
        }
        return schemas;
    }

    public RealmList<Activity> getActivityRealmList() {
        return activityRealmList;
    }

    public void setActivityRealmList(RealmList<Activity> activityRealmList) {
        this.activityRealmList = activityRealmList;
    }

    public Long getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
    }

    public String getSchemeCode() {
        return schemeCode;
    }

    public void setSchemeCode(String schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getSchemeNameEn() {
        return schemeNameEn;
    }

    public void setSchemeNameEn(String schemeNameEn) {
        this.schemeNameEn = schemeNameEn;
    }

    public String getSchemeNameHi() {
        return schemeNameHi;
    }

    public void setSchemeNameHi(String schemeNameHi) {
        this.schemeNameHi = schemeNameHi;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return schemeNameEn;
    }
}
