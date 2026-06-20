package se.magnus.microservices.core.telemetry.persistence;

import static java.lang.String.format;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

  @Document(collection = "telemetry")
  public class TelemetryEntity {

  @Id
  private String id;

  @Version
  private Integer version;

  @Indexed
  private int deviceId;

  private int readingId;
  private String sensorType;
  private int value;
  private String unit;

  public TelemetryEntity() {
  }

  public TelemetryEntity(String id, Integer version, int deviceId, int readingId, String sensorType, int value, String unit) {
      this.id = id;
      this.version = version;
      this.deviceId = deviceId;
      this.readingId = readingId;
      this.sensorType = sensorType;
      this.value = value;
      this.unit = unit;
    }

    public String toString(){
    return format ("TelemetryEntity: %s/%d: ", deviceId, readingId);
    }
    public String getId() {
      return id;
    }

    public Integer getVersion() {
      return version;
    }

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

    public void setId(String id) {
      this.id = id;
    }

    public void setVersion(Integer version) {
      this.version = version;
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
  }