package se.magnus.microservices.core.alert.persistence;

import static java.lang.String.format;

import jakarta.persistence.*;

@Entity
@Table(name = "alerts", indexes = { @Index(name = "alerts_unique_idx", unique = true, columnList = "incidentId,alertId") })
public class AlertEntity {

  @Id @GeneratedValue
  private int id;

  @Version
  private int version;

  private int incidentId;
  private int alertId;
  private String author;
  private String subject;
  private String content;

  public AlertEntity() {
  }

  public AlertEntity(int incidentId, int alertId, String author, String subject, String content) {
    this.incidentId = incidentId;
    this.alertId = alertId;
    this.author = author;
    this.subject = subject;
    this.content = content;
  }

  @Override
  public String toString() {
    return format("AlertEntity: %s/%d", incidentId, alertId);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public int getIncidentId() {
    return incidentId;
  }

  public void setIncidentId(int incidentId) {
    this.incidentId = incidentId;
  }

  public int getAlertId() {
    return alertId;
  }

  public void setAlertId(int alertId) {
    this.alertId = alertId;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
