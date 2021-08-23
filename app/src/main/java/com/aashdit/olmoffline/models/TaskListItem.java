package com.aashdit.olmoffline.models;

import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TaskListItem extends RealmObject {

    @PrimaryKey
    public String unique;
    public Long month;
    public Long year;
    public Long schemeId;
    public Long activityId;
    public Long entityId;
    public String levelCode;
    public String wfStatus;
    public String monthName;
    public String entityName;
    public String entityLine1;
    public String properties;
    public String schemeName;
    public String activityName;
    public String wfStatusIndicator;
    public boolean isUpdated;

    public static TaskListItem parseTaskList(JSONObject object){
        TaskListItem item = new TaskListItem();
        item.month = object.optLong("month");
        item.year = object.optLong("year");
        item.schemeId = object.optLong("schemeId");
        item.activityId = object.optLong("activityId");
        item.entityId = object.optLong("entityId");
        item.levelCode = object.optString("levelCode");
        item.wfStatus = object.optString("wfStatus");
        item.monthName = object.optString("monthName");
        item.entityName = object.optString("entityName");
        item.entityLine1 = object.optString("entityLine1");
        item.properties = object.optString("properties");
        item.schemeName = object.optString("schemeName");
        item.activityName = object.optString("activityName");
        item.wfStatusIndicator = object.optString("wfStatusIndicator");
        item.isUpdated = false;

        //schemeId+"_"+activityId+"_"+monthId+"_"+year+"_"+entityId;
        item.unique = item.schemeId+"_"+item.activityId+item.month+"_"+item.year+"_"+item.entityId;
        return item;
    }
}
