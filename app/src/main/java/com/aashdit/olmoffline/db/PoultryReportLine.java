package com.aashdit.olmoffline.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Manabendu on 05/08/21
 */
public class PoultryReportLine extends RealmObject {


    public Long schemeId;
    public Long activityId;
    public int year;
    public int monthId;
    public Long entityId;
    public String entityTypeCode;
    public String numBirdBought;
    public String numBirdSold;
    public String numBirdDead;
    public String numEggsSold;
    public String birdIncome;
    public String birdExpenditure;
    public String birdSalesIncome;
    public String eggSalesIncome;
    public String dewormingFrequency;
    public String remarks;
    public boolean regularDeworming;
    public boolean isSynced;
    @PrimaryKey
    public String unique = schemeId+"_"+activityId+"_"+monthId+"_"+year;






}
