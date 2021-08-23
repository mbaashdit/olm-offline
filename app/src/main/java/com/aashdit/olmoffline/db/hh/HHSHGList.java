package com.aashdit.olmoffline.db.hh;

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Manabendu on 06/08/21
 */
public class HHSHGList extends RealmObject {

    public Long orgId;
    public String orgName;
    public RealmList<HhList> houseHold;

    public Long schemeId;
    public Long activityId;

    public HHSHGList() {
    }

    public static HHSHGList parseShgList(JSONObject object,Long _schemeId,Long _activityId){
        HHSHGList shgList= new HHSHGList();
        shgList.orgId = object.optLong("orgId");
        shgList.orgName =object.optString("orgName");
        shgList.schemeId = _schemeId;
        shgList.activityId =_activityId;

        shgList.houseHold = new RealmList<>();
        JSONArray hhArray = object.optJSONArray("houseHold");
        if (hhArray != null && hhArray.length() > 0){
            for (int i = 0; i < hhArray.length(); i++) {
                HhList hhList = HhList.parseHhList(hhArray.optJSONObject(i),_schemeId,_activityId,shgList.orgId);
                shgList.houseHold.add(hhList);
            }
        }

        return shgList;
    }
}
