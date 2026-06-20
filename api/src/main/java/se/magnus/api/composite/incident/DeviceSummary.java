package se.magnus.api.composite.incident;

public class DeviceSummary {

  private final int deviceId;
  private final String author;
  private final int rate;
  private final String content;

  public DeviceSummary() {
    this.deviceId = 0;
    this.author = null;
    this.rate = 0;
    this.content = null;
  }

  public DeviceSummary(int deviceId, String author, int rate, String content) {
    this.deviceId = deviceId;
    this.author = author;
    this.rate = rate;
    this.content = content;
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
}
