package se.magnus.api.core.device;

public class Device {
  private int incidentId;
  private int deviceId;
  private String author;
  private int rate;
  private String content;
  private String serviceAddress;

  public Device() {
    incidentId = 0;
    deviceId = 0;
    author = null;
    rate = 0;
    content = null;
    serviceAddress = null;
  }

  public Device(
    int incidentId,
    int deviceId,
    String author,
    int rate,
    String content,
    String serviceAddress) {

    this.incidentId = incidentId;
    this.deviceId = deviceId;
    this.author = author;
    this.rate = rate;
    this.content = content;
    this.serviceAddress = serviceAddress;
  }

  public int getIncidentId() {
    return incidentId;
  }

  public int getDeviceId() {
    return deviceId;
  }

  public String getAuthor() {
    return author;
  }

  public int getRate() {
    return rate;
  }

  public String getContent() {
    return content;
  }

  public String getServiceAddress() {
    return serviceAddress;
  }

  public void setIncidentId(int incidentId) {
    this.incidentId = incidentId;
  }

  public void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setRate(int rate) {
    this.rate = rate;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setServiceAddress(String serviceAddress) {
    this.serviceAddress = serviceAddress;
  }
}
