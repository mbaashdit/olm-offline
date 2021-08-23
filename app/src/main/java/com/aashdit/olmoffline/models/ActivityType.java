package com.aashdit.olmoffline.models;

import com.aashdit.olmoffline.db.hh.ActivityHHSearch;
import com.aashdit.olmoffline.db.shg.ActivitySHGSearch;
import com.aashdit.olmoffline.utils.Constant;

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ActivityType extends RealmObject {

    public RealmList<Activity> activityRealmList;
    public RealmList<ActivitySHGSearch> activitySHGSearchRealmList;
    public RealmList<ActivityHHSearch> activityHHSearchRealmList;
    @PrimaryKey
    private Long activityId;
    private String activityCode;
    private String activityTypeDesc;
    private boolean isActive;

    public static ActivityType parseActivityType(JSONObject object, Long schemeId, String userType, String dataType) {
        ActivityType activity = new ActivityType();
        activity.activityId = object.optLong("activityTypeId");
        activity.activityCode = object.optString("activityTypeCode");
        activity.activityTypeDesc = object.optString("activityTypeDesc");
        activity.isActive = object.optBoolean("isActive");


        if (userType.equals(Constant.PMITRA)) {
            activity.activityRealmList = new RealmList<>();
            Activity _activity = new Activity();
            _activity.setActivityCode("ACTIVITY");
            _activity.setActivityId(0L);
            _activity.setActivityNameEn("Select Activity");
            _activity.setActivityNameHi("Select Activity");
            JSONArray activityJArray = object.optJSONArray("activity");
            if (activityJArray != null && activityJArray.length() > 0) {
                activity.activityRealmList.add(_activity);
                for (int i = 0; i < activityJArray.length(); i++) {
                    activity.activityRealmList.add(Activity.parseActivity(activityJArray.optJSONObject(i), schemeId, activity.activityId));
                }
            }
        } else {
            if (dataType.equals("shg")) {
                activity.activitySHGSearchRealmList = new RealmList<>();
                ActivitySHGSearch activitySHGSearch = new ActivitySHGSearch();
                activitySHGSearch.activityId = 0L;
                activitySHGSearch.activityCode = "ACTIVITY";
                activitySHGSearch.activityNameEn = "Select Activity";
                activitySHGSearch.activityNameHi = "Select Activity";
                activity.activitySHGSearchRealmList.add(activitySHGSearch);

                JSONArray activityJArray = object.optJSONArray("activity");
                if (activityJArray != null && activityJArray.length() > 0) {
                    for (int i = 0; i < activityJArray.length(); i++) {
                        activity.activitySHGSearchRealmList.add(ActivitySHGSearch.parseActivitySearch(activityJArray.optJSONObject(i), schemeId, activity.activityId));
                    }
                }

            } else if (dataType.equals("hh")) {
                activity.activityHHSearchRealmList = new RealmList<>();
                ActivityHHSearch activitySHGSearch = new ActivityHHSearch();
                activitySHGSearch.activityId = 0L;
                activitySHGSearch.activityCode = "ACTIVITY";
                activitySHGSearch.activityNameEn = "Select Activity";
                activitySHGSearch.activityNameHi = "Select Activity";
                activity.activityHHSearchRealmList.add(activitySHGSearch);

                JSONArray activityJArray = object.optJSONArray("activity");
                for (int i = 0; i < activityJArray.length(); i++) {
                    activity.activityHHSearchRealmList.add(ActivityHHSearch.parseActivitySearch(activityJArray.optJSONObject(i), schemeId, activity.activityId));
                }
            }
            JSONArray activityJArray = object.optJSONArray("activity");
            if (activityJArray != null && activityJArray.length() > 0) {
                activity.activityRealmList = new RealmList<>();
                Activity _activity = new Activity();
                _activity.setActivityCode("ACTIVITY");
                _activity.setActivityId(0L);
                _activity.setActivityNameEn("Select Activity");
                _activity.setActivityNameHi("Select Activity");
                activity.activityRealmList.add(_activity);
                for (int i = 0; i < activityJArray.length(); i++) {
                    activity.activityRealmList.add(Activity.parseActivity(activityJArray.optJSONObject(i), schemeId, activity.activityId));
                }

            }
        }
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

    public String getActivityTypeDesc() {
        return activityTypeDesc;
    }

    public void setActivityTypeDesc(String activityTypeDesc) {
        this.activityTypeDesc = activityTypeDesc;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return activityTypeDesc;
    }
}
