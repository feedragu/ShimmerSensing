package com.example.shimmersensing.Utilities;

public class ShimmerData {
    private double PPG, gsrResistance, gsrConductance, accelerometer;
    private long timestamp_shimmer;

    public double getPPG() {
        return PPG;
    }

    public void setPPG(double PPG) {
        this.PPG = PPG;
    }

    public double getGsrResistance() {
        return gsrResistance;
    }

    public void setGsrResistance(double gsrResistance) {
        this.gsrResistance = gsrResistance;
    }

    public double getGsrConductance() {
        return gsrConductance;
    }

    public void setGsrConductance(double gsrConductance) {
        this.gsrConductance = gsrConductance;
    }

    public double getAccelerometer() {
        return accelerometer;
    }

    public void setAccelerometer(double accelerometer) {
        this.accelerometer = accelerometer;
    }

    public ShimmerData(double PPG, double gsrResistance, double gsrConductance, double accelerometer, long timestamp_shimmer) {
        this.PPG = PPG;
        this.gsrResistance = gsrResistance;
        this.gsrConductance = gsrConductance;
        this.accelerometer = accelerometer;
        this.timestamp_shimmer = timestamp_shimmer;
    }
}
