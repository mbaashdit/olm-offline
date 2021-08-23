package com.aashdit.olmoffline.models;

public class Month {

    private String monthName;
    private String yearName;

    public Month() {
    }

    public Month(String monthName) {
        this.monthName = monthName;
    }

    public Month(String monthName, String yearName) {
        this.monthName = monthName;
        this.yearName = yearName;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public String getYearName() {
        return yearName;
    }

    public void setYearName(String yearName) {
        this.yearName = yearName;
    }
}
