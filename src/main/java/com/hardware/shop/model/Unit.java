package com.hardware.shop.model;

public class Unit {

    private int id;
    private String unitName;
    private String shortName;

    public Unit() {
    }

    public Unit(int id, String unitName, String shortName) {
        this.id = id;
        this.unitName = unitName;
        this.shortName = shortName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}