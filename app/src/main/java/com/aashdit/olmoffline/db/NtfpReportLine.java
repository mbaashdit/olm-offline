package com.aashdit.olmoffline.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 24/08/21
 */
public class NtfpReportLine extends RealmObject {

    public Long schemeId;
    public Long activityId;
    public int year;
    public int monthId;
    public Long entityId;
    public String reportingLevelCode;
    public String remarks;
    public String qtySold;
    public String qtyProduced;
    public String valueAddition;
    public String totalInputCost;
    public String seasonalIncome;
    public String totalGrossIncome;
    public boolean isSynced;
    @PrimaryKey
    public String unique = schemeId+"_"+activityId+"_"+monthId+"_"+year;


}
