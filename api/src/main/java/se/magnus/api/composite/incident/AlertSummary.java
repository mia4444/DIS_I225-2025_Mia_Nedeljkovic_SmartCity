package se.magnus.api.composite.incident;

public class AlertSummary {

  private final int alertId;
  private final String author;
  private final String subject;
  private final String content;

  public AlertSummary() {
    this.alertId = 0;
    this.author = null;
    this.subject = null;
    this.content = null;
  }

  public AlertSummary(int alertId, String author, String subject, String content) {
    this.alertId = alertId;
    this.author = author;
    this.subject = subject;
    this.content = content;
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
}
