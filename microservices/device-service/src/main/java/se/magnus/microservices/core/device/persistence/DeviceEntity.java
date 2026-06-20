package se.magnus.microservices.core.device.persistence;
import static java.lang.String.format;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public class DeviceEntity {

  @Id private String id;

  @Version private Integer version;

  @Indexed
  private int incidentId;

  private int deviceId;
  private String author;
  private int rate;
  private String content;

  public DeviceEntity() {}

  public DeviceEntity(int incidentId, int deviceId, String author, int rate, String content) {
    this.incidentId = incidentId;
    this.deviceId = deviceId;
    this.author = author;
    this.rate = rate;
    this.content = content;
  }

  @Override
  public String toString() {
    return format("DeviceEntity: %s", deviceId);
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public Integer getVersion() { return version; }
  public void setVersion(Integer version) { this.version = version; }

  public int getIncidentId() { return incidentId; }
  public void setIncidentId(int incidentId) { this.incidentId = incidentId; }

  public int getDeviceId() { return deviceId; }
  public void setDeviceId(int deviceId) { this.deviceId = deviceId; }

  public String getAuthor() { return author; }
  public void setAuthor(String author) { this.author = author; }

  public int getRate() { return rate; }
  public void setRate(int rate) { this.rate = rate; }

  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
}
