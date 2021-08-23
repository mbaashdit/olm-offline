package com.aashdit.olmoffline.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 10/08/21
 */
public class DairyReportLine extends RealmObject {

    public Long schemeId;
    public Long activityId;
    public int year;
    public int monthId;
    public Long entityId;
    public String entityTypeCode;
    public String remarks;
    public String numCowDead;
    public String numCowSold;
    public String numCowBought;
    public String monthlyCowMilkProduced;
    public String monthlyCowMilkSold;
    public String numCowVaccinated;
    public String milkIncome;
    public String numDewormed;
    public String numCalfBorn;
    public String numCalfDead;
    public String numCalfBought;
    public String numCalfSold;
    public Double totalExpenditure;
    public Double totalIncome;
    public boolean regularVaccinationDone;
    public boolean regularDewormingDone;
    public boolean isSynced;
    @PrimaryKey
    public String unique = schemeId+"_"+activityId+"_"+monthId+"_"+year;


}
