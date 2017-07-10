package com.cht.iot.chtiotapp.other;

import android.graphics.Bitmap;

/**
 * Created by Arron on 2016/11/2.
 */

public class SensorItem {

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;

    }

    public static final String TOGGLEBTN= "TOGGLEBTN";
    public static final String IMAGEBTN= "IMAGEBTN";
    public static final String BUTTON = "BUTTON";

    private String sensorName;
    private int sensorIcon;
    private boolean sensorToggleBtn;
    private String sensorTextOrNumber;

    public Bitmap getSensorImageBtn() {
        return sensorImageBtn;
    }

    public void setSensorImageBtn(Bitmap sensorImageBtn) {
        this.sensorImageBtn = sensorImageBtn;
    }

    private Bitmap sensorImageBtn;

    public boolean isSensorToggleBtn() {
        return sensorToggleBtn;
    }

    public void setSensorToggleBtn(boolean sensorToggleBtn) {
        this.sensorToggleBtn = sensorToggleBtn;
    }

    public String getSensorTextOrNumber() {
        return sensorTextOrNumber;
    }

    public void setSensorTextOrNumber(String sensorTextOrNumber) {
        this.sensorTextOrNumber = sensorTextOrNumber;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public int getSensorIcon() {
        return sensorIcon;
    }

    public void setSensorIcon(int sensorIcon) {
        this.sensorIcon = sensorIcon;
    }
}
