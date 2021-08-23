package com.aashdit.olmoffline.db.hh;

import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 05/08/21
 */
public class HhList extends RealmObject {

    @PrimaryKey
    public String uniqueHhId;
    public Long orgId;
    public String orgName;
    public Long schemeId;
    public Long activityTypeId;
    public Long activityId;
    public Long shgId;

    public HhList() {
    }

    public static HhList parseHhList(JSONObject object,Long schemeId,Long activityId,Long shgId){
        HhList shgList= new HhList();
        shgList.orgId = object.optLong("orgId");
        shgList.orgName =object.optString("orgName");
        shgList.shgId = shgId;
        shgList.activityId = activityId;
        shgList.schemeId = schemeId;

        shgList.uniqueHhId = schemeId+"_"+activityId+"_"+shgId+"_"+ shgList.orgId;

        return shgList;
    }
}
