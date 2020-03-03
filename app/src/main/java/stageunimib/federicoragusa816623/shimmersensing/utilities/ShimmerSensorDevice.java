package stageunimib.federicoragusa816623.shimmersensing.utilities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShimmerSensorDevice that = (ShimmerSensorDevice) o;
        return deviceName.equals(that.deviceName) &&
                macAddress.equals(that.macAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceName, macAddress, sampleRate, lastUse);
    }

    public Date getLastUse() {
        return lastUse;
    }
}
