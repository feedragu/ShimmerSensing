package com.example.shimmersensing.global;

import android.app.Application;
import android.util.Log;

import com.example.shimmersensing.utilities.ShimmerSensorDevice;
import com.example.shimmersensing.utilities.ShimmerTrial;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class GlobalValues extends Application {
    private String name, surname, date;
    private ArrayList<ShimmerTrial> shimmerTrialArrayList;
    private ShimmerSensorDevice ssd;

    public void setSsd(ShimmerSensorDevice ssd) {
        Log.i(TAG, "setSsd: "+ssd.getDeviceName());
        this.ssd = ssd;
    }

    public ShimmerSensorDevice getSsd() {
        return ssd;
    }

    public GlobalValues() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setShimmerTrialArrayList(ArrayList<ShimmerTrial> shimmerTrialArrayList) {
        this.shimmerTrialArrayList = shimmerTrialArrayList;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<ShimmerTrial> getShimmerTrialArrayList() {
        return shimmerTrialArrayList;
    }
}
