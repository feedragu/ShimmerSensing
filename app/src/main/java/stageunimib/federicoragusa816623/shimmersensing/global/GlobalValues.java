package stageunimib.federicoragusa816623.shimmersensing.global;

import android.app.Application;

import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerSensorDevice;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerTrial;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.driver.ShimmerDevice;

import java.util.ArrayList;

public class GlobalValues extends Application {
    private String name, surname, date;
    private String _id;
    private ArrayList<ShimmerTrial> shimmerTrialArrayList;
    private ShimmerSensorDevice ssd;

    public GlobalValues() {
    }


    public ShimmerSensorDevice getSsd() {
        return ssd;
    }

    public void setSsd(ShimmerSensorDevice ssd) {
        this.ssd = ssd;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_id() {
        return _id;
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
