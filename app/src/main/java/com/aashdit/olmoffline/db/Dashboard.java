package com.aashdit.olmoffline.db;

import io.realm.RealmObject;

/**
 * Created by Manabendu on 01/08/21
 */
public class Dashboard extends RealmObject {
    public String district;
    public String block;
    public String gp;
    public String villages;
    public String userName;
    public String lastLogin;
    public String labelValue;
    public String label;
    public Long egReported;
    public Long pgReported;
    public Long clfReported;
    public Long shgReported;
    public Long hhReported;
    public Long activitiesCovered;

}
