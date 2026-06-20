package se.magnus.microservices.core.user.persistence;

import static java.lang.String.format;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "incidents")
public class UserEntity {

  @Id private String id;

  @Version private Integer version;

  @Indexed(unique = true)
  private int incidentId;

  private String name;
  private int weight;

  public UserEntity() {}

  public UserEntity(int incidentId, String name, int weight) {
    this.incidentId = incidentId;
    this.name = name;
    this.weight = weight;
  }

  @Override
  public String toString() {
    return format("IncidentCoreEntity: %s", incidentId);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public int getIncidentId() {
    return incidentId;
  }

  public void setIncidentId(int incidentId) {
    this.incidentId = incidentId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }
}
