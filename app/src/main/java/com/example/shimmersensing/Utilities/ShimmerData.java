package com.example.shimmersensing.Utilities;

public class ShimmerData {
    private double PPG, gsrConductance, accelerometer_x, accelerometer_y, accelerometer_z,
            gyroscope_x, gyroscope_y, gyroscope_z, magnetometer_x, magnetometer_y, magnetometer_z;
    private double timestamp_shimmer;


    public ShimmerData(double PPG, double gsrConductance, double accelerometer_x, double accelerometer_y,
                       double accelerometer_z, double gyroscope_x, double gyroscope_y,
                       double gyroscope_z, double magnetometer_x, double magnetometer_y,
                       double magnetometer_z, double timestamp_shimmer) {
        this.PPG = PPG;
        this.gsrConductance = gsrConductance;
        this.accelerometer_x = accelerometer_x;
        this.accelerometer_y = accelerometer_y;
        this.accelerometer_z = accelerometer_z;
        this.gyroscope_x = gyroscope_x;
        this.gyroscope_y = gyroscope_y;
        this.gyroscope_z = gyroscope_z;
        this.magnetometer_x = magnetometer_x;
        this.magnetometer_y = magnetometer_y;
        this.magnetometer_z = magnetometer_z;
        this.timestamp_shimmer = timestamp_shimmer;
    }

    @Override
    public String toString() {
        return "ShimmerData{" +
                "PPG=" + PPG +
                ", gsrConductance=" + gsrConductance +
                ", accelerometer_x=" + accelerometer_x +
                ", accelerometer_y=" + accelerometer_y +
                ", accelerometer_z=" + accelerometer_z +
                ", gyroscope_x=" + gyroscope_x +
                ", gyroscope_y=" + gyroscope_y +
                ", gyroscope_z=" + gyroscope_z +
                ", magnetometer_x=" + magnetometer_x +
                ", magnetometer_y=" + magnetometer_y +
                ", magnetometer_z=" + magnetometer_z +
                ", timestamp_shimmer=" + timestamp_shimmer +
                '}';
    }

    public double getPPG() {
        return PPG;
    }

    public double getGsrConductance() {
        return gsrConductance;
    }

    public double getAccelerometer_x() {
        return accelerometer_x;
    }

    public double getAccelerometer_y() {
        return accelerometer_y;
    }

    public double getAccelerometer_z() {
        return accelerometer_z;
    }

    public double getGyroscope_x() {
        return gyroscope_x;
    }

    public double getGyroscope_y() {
        return gyroscope_y;
    }

    public double getGyroscope_z() {
        return gyroscope_z;
    }

    public double getMagnetometer_x() {
        return magnetometer_x;
    }

    public double getMagnetometer_y() {
        return magnetometer_y;
    }

    public double getMagnetometer_z() {
        return magnetometer_z;
    }

    public double getTimestamp_shimmer() {
        return timestamp_shimmer;
    }


}
