package com.aashdit.olmoffline.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 17/08/21
 */
public class NonFarmReportLine extends RealmObject {
    public Long schemeId;
    public Long activityId;
    public int reportingYear;
    public int monthId;
    public Long entityId;
    public String entityTypeCode;
    public String remarks;
    public boolean hasAquiredTraining;
    public Long skillTypeId;
    public String totalSales;
    public String totalExpenditure;
    public String totalProfit;
    public boolean isSynced;
    @PrimaryKey
    public String unique = schemeId + "_" + activityId + "_" + monthId + "_" + reportingYear + "_" + entityId;
}
