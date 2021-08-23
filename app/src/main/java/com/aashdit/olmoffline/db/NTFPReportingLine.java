package com.aashdit.olmoffline.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 17/08/21
 */
public class NTFPReportingLine extends RealmObject {
    public Long schemeId;
    public Long activityId;
    public int year;
    public int monthId;
    public Long entityId;
    public String entityTypeCode;
    public String qtySold;
    public String qtyProduced;
    public String valueAddition;
    public String remarks;
    public Double totalInputCost;
    public Double seasonalIncome;
    public Double totalGrossIncome;
    public boolean isSynced;
    @PrimaryKey
    public String unique = schemeId+"_"+activityId+"_"+monthId+"_"+year+"_"+entityId;
}
