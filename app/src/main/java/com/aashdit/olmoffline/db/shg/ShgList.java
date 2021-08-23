package com.aashdit.olmoffline.db.shg;

import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 05/08/21
 */
public class ShgList extends RealmObject {

    @PrimaryKey
    public String uniqueShgId;
    public Long orgId;
    public String orgName;
    public Long schemeId;
    public Long activityTypeId;
    public Long activityId;
    public ShgList() {
    }

    public static ShgList parseShgList(JSONObject object,Long schemeId,Long activityTypeId,Long activityId){
        ShgList shgList= new ShgList();
        shgList.orgId = object.optLong("orgId");
        shgList.orgName =object.optString("orgName");
        shgList.schemeId = schemeId;
        shgList.activityId = activityId;
        shgList.activityTypeId = activityTypeId;
        shgList.uniqueShgId = schemeId+"_"+activityTypeId+"_"+activityId+"_"+ shgList.orgId;

//        shgList.houseHold = new RealmList<>();
//        JSONArray hhArray = object.optJSONArray("houseHold");
//        if (hhArray != null && hhArray.length() > 0){
//            for (int i = 0; i < hhArray.length(); i++) {
//                HhList hhList = HhList.parseHhList(hhArray.optJSONObject(i));
//                shgList.houseHold.add(hhList);
//            }
//        }

        return shgList;
    }
}
