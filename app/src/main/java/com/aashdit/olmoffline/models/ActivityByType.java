package com.aashdit.olmoffline.models;

import org.json.JSONObject;

public class ActivityByType {
    private Long activityId;
    private String activityCode;
    private String activityNameHi;
    private String activityNameEn;
    private boolean isActive;


    public static ActivityByType parseActivityByType(JSONObject object) {
        ActivityByType activity = new ActivityByType();
        activity.activityId = object.optLong("activityId");
        activity.activityCode = object.optString("activityCode");
        activity.activityNameHi = object.optString("activityNameHi");
        activity.activityNameEn = object.optString("activityNameEn");
        activity.isActive = object.optBoolean("isActive");
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


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return  activityNameEn ;
    }
}
