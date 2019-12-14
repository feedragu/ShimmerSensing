package com.example.shimmersensing.utilities;

import java.io.Serializable;
import java.util.Date;

public class ShimmerSensorDevice implements Serializable {

    private String deviceName, macAddress;
    private double sampleRate;
    private Date lastUse;



    public ShimmerSensorDevice(String deviceName, String macAddress, double sampleRate, Date lastUse) {
        this.deviceName = deviceName;
        this.macAddress = macAddress;
        this.sampleRate = sampleRate;
        this.lastUse = lastUse;
    }

    @Override
    public String toString() {
        return "ShimmerSensorDevice{" +
                "deviceName='" + deviceName + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", sampleRate=" + sampleRate +
                ", lastUse=" + lastUse +
                '}';
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public double getSampleRate() {
        return sampleRate;
    }

    public Date getLastUse() {
        return lastUse;
    }
}
