package com.aashdit.olmoffline.models;

import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Entries extends RealmObject {

    public String reportingLevel;
    public String activityName;

    private String arrowType;
    private String colorCode;
    private String growthValue;
    private String orgName;
    private String bankBalance;
    public String profit;

    @PrimaryKey
    private Long orgId;
    private Long goats;
    private Long bucks;
    private Long birds;
    private Long fingerLings;
    private Long fish;
    private Long cows;
    private Long calf;
//    private Long buffalo;
//    private Long buffaloCalf;


    public  static  Entries parseEntries(JSONObject object, String reportingLevel, String type){
        Entries entries = new Entries();

        entries.reportingLevel = reportingLevel;
        entries.activityName = type;

        entries.arrowType = object.optString("arrowType");
        entries.colorCode = object.optString("colorCode");
        entries.growthValue = object.optString("growthValue");
        entries.orgName = object.optString("orgName");
        entries.bankBalance = object.optString("bankBalance");
        entries.profit = object.optString("profit");
        entries.orgId = object.optLong("orgId");
        entries.goats = object.optLong("goats");
        entries.bucks = object.optLong("bucks");
        entries.birds = object.optLong("birds");
        entries.fingerLings = object.optLong("fingerLings");
        entries.fish = object.optLong("fish");
        entries.cows = object.optLong("cows");
        entries.calf = object.optLong("calf");
//        entries.buffalo = object.optLong("buffalo");
//        entries.buffaloCalf = object.optLong("buffaloCalf");
        return  entries;
    }

    public Long getCows() {
        return cows;
    }

    public void setCows(Long cows) {
        this.cows = cows;
    }

    public Long getCalf() {
        return calf;
    }

    public void setCalf(Long calf) {
        this.calf = calf;
    }

    public Long getFingerLings() {
        return fingerLings;
    }

    public void setFingerLings(Long fingerLings) {
        this.fingerLings = fingerLings;
    }

    public Long getFish() {
        return fish;
    }

    public void setFish(Long fish) {
        this.fish = fish;
    }

    public Long getBirds() {
        return birds;
    }

    public void setBirds(Long birds) {
        this.birds = birds;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getArrowType() {
        return arrowType;
    }

    public void setArrowType(String arrowType) {
        this.arrowType = arrowType;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getGrowthValue() {
        return growthValue;
    }

    public void setGrowthValue(String growthValue) {
        this.growthValue = growthValue;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(String bankBalance) {
        this.bankBalance = bankBalance;
    }

    public Long getGoats() {
        return goats;
    }

    public void setGoats(Long goats) {
        this.goats = goats;
    }

    public Long getBucks() {
        return bucks;
    }

    public void setBucks(Long bucks) {
        this.bucks = bucks;
    }
}
