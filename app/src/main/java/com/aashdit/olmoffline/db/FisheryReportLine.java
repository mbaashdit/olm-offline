package com.aashdit.olmoffline.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 10/08/21
 */
public class FisheryReportLine extends RealmObject {

    public Long schemeId;
    public Long activityId;
    public int year;
    public int monthId;
    public Long entityId;
    public String entityTypeCode;
    public String remarks;
    public String numFingerlingsHarvested;
    public String tableSizeFishHarvested;
    public String fingerlingsSoldQty;
    public String tableSizeFishSold;
    public Double totalExpenditure;
    public Double totalIncome;
    public Double fingerlingIncome;
    public Double tableFishIncome;

    public boolean isSynced;
    @PrimaryKey
    public String unique = schemeId+"_"+activityId+"_"+monthId+"_"+year;

}
