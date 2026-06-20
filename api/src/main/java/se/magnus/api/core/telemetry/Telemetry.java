package se.magnus.api.core.telemetry;

public class Telemetry {
    private int deviceId;
    private int readingId;
    private String sensorType;
    private int value;
    private String unit;
    private String serviceAddress;

    public Telemetry(){} //prazan, default konstruktor (da ga ja nisam napravila Java bi to uradila u pozadini)

    //Uradila putem Generate-a (kao Source u Eclipse-a, ali mora da se drži Ctrl ovde da se izabere više property-ja)
    public Telemetry(int deviceId, int readingId, String sensorType, int value, String unit, String serviceAddress) {
        this.deviceId = deviceId;
        this.readingId = readingId;
        this.sensorType = sensorType;
        this.value = value;
        this.unit = unit;
        this.serviceAddress = serviceAddress;
    }

    //Isto ću uraditi za getere i setere


    public int getDeviceId() {
        return deviceId;
    }

    public int getReadingId() {
        return readingId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public int getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public void setReadingId(int readingId) {
        this.readingId = readingId;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}