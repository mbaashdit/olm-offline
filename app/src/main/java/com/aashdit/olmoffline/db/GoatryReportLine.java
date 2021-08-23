package com.aashdit.olmoffline.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 03/08/21
 */
public class GoatryReportLine extends RealmObject {

    public Long schemeId;
    public Long activityId;
    public int year;
    public int monthId;
    public Long entityId;
    public String entityTypeCode;
    public String numGoatSold;
    public String totalIncome;
    public String numGoatBought;
    public String totalExpenditure;
    public String numGoatBorn;
    public String numGoatDead;
    public String numBuckSold;
    public String numBuckBought;
    public String numBuckBorn;
    public String numBuckDead;
    public String numVaccinated;
    public String numDewormed;
    public String remarks;
    public boolean regularVaccinated;
    public boolean regularDeworming;
    public boolean bucksTied;
    public boolean isSynced;
    @PrimaryKey
    public String unique = schemeId+"_"+activityId+"_"+monthId+"_"+year+"_"+entityId;

}
