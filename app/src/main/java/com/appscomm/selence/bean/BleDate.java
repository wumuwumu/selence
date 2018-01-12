package com.appscomm.selence.bean;

import com.google.gson.annotations.SerializedName;


public class BleDate {

    /**
     * switch : 0
     * key : 1
     * gear : 0
     */
    @SerializedName("switch")
    private String switchX;
    private String key;
    private String gear;

    public String getSwitchX() {
        return switchX;
    }

    public void setSwitchX(String switchX) {
        this.switchX = switchX;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getGear() {
        return gear;
    }

    public void setGear(String gear) {
        this.gear = gear;
    }
}
