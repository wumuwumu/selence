package com.appscomm.selence.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/10/24.
 */

public class BleReslutDate {

    /**
     * power : 90
     * gear : 0
     * charge : 0
     */

    private String power;
    private String gear;
    private String charge;

    @SerializedName("switch")
    private String switchx;

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getGear() {
        return gear;
    }

    public void setGear(String gear) {
        this.gear = gear;
    }

    public String getCharge() {
        return charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public String getSwitchx() {
        return switchx;
    }

    public void setSwitchx(String switchx) {
        this.switchx = switchx;
    }
}
