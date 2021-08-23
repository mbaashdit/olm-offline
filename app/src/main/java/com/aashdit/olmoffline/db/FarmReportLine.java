package com.aashdit.olmoffline.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 17/08/21
 */
public class FarmReportLine extends RealmObject {

    public Long schemeId;
    public Long activityId;
    public int year;
    public int monthId;
    public Long entityId;
    public String entityTypeCode;
    public String quantitySold;
    public String quantityProduced;
    public String remarks;
    public String searchTerm;
    public String seasonalIncome;
    public String totalExpenditure;
    public String totalIncome;
    public boolean isSynced;
    @PrimaryKey
    public String unique = schemeId + "_" + activityId + "_" + monthId + "_" + year + "_" + entityId;

}
