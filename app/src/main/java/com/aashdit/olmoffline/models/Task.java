package com.aashdit.olmoffline.models;

import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 10/01/21
 */
public class Task extends RealmObject {

    @PrimaryKey
    public String unique;
    public Long month;
    public Long year;
    public Long schemeId;
    public Long activityId;
    public int clfCount;
    public int shgCount;
    public int pgCount;
    public int egCount;
    public int householdCount;
    public String taskStatus;
    public String schemeName;
    public String activityName;
    public String hashu;
    public String monthCode;

    public static Task parseTask(JSONObject object){
        Task task = new Task();
        task.month = object.optLong("month");
        task.year = object.optLong("year");
        task.schemeId = object.optLong("schemeId");
        task.activityId = object.optLong("activityId");
        task.clfCount = object.optInt("clfCount");
        task.shgCount = object.optInt("shgCount");
        task.pgCount = object.optInt("pgCount");
        task.egCount = object.optInt("egCount");
        task.householdCount = object.optInt("householdCount");
        task.taskStatus = object.optString("taskStatus");
        task.schemeName = object.optString("schemeName");
        task.activityName = object.optString("activityName");
        task.hashu = object.optString("hashu");
        task.monthCode = object.optString("monthCode");

        task.unique = task.month+"_"+task.year+"_"+task.schemeId+"_"+task.activityId;
        return task;
    }
}
