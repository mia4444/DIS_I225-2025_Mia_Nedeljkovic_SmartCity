package se.magnus.api.core.alert;

public class Alert {
  private int incidentId;
  private int alertId;
  private String author;
  private String subject;
  private String content;
  private String serviceAddress;

  public Alert() {
    incidentId = 0;
    alertId = 0;
    author = null;
    subject = null;
    content = null;
    serviceAddress = null;
  }

  public Alert(
    int incidentId,
    int alertId,
    String author,
    String subject,
    String content,
    String serviceAddress) {

    this.incidentId = incidentId;
    this.alertId = alertId;
    this.author = author;
    this.subject = subject;
    this.content = content;
    this.serviceAddress = serviceAddress;
  }

  public int getIncidentId() {
    return incidentId;
  }

  public int getAlertId() {
    return alertId;
  }

  public String getAuthor() {
    return author;
  }

  public String getSubject() {
    return subject;
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

  public void setAlertId(int alertId) {
    this.alertId = alertId;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setServiceAddress(String serviceAddress) {
    this.serviceAddress = serviceAddress;
  }
}
