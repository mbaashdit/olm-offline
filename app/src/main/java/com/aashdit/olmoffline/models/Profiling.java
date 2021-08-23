package com.aashdit.olmoffline.models;

public class Profiling {
    private String name;
    private String amount;
    private String noColor;
    private String charColorBg;

    public Profiling(String name, String amount, String noColor, String charColorBg) {
        this.name = name;
        this.amount = amount;
        this.noColor = noColor;
        this.charColorBg = charColorBg;
    }

    public Profiling(String name, String amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getNoColor() {
        return noColor;
    }

    public void setNoColor(String noColor) {
        this.noColor = noColor;
    }

    public String getCharColorBg() {
        return charColorBg;
    }

    public void setCharColorBg(String charColorBg) {
        this.charColorBg = charColorBg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
