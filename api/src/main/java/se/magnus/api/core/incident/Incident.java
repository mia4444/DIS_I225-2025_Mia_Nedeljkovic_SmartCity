package se.magnus.api.core.incident;

public class Incident {
  private int incidentId;
  private String name;
  private int weight;
  private String serviceAddress;

  public Incident() {
    incidentId = 0;
    name = null;
    weight = 0;
    serviceAddress = null;
  }

  public Incident(int incidentId, String name, int weight, String serviceAddress) {
    this.incidentId = incidentId;
    this.name = name;
    this.weight = weight;
    this.serviceAddress = serviceAddress;
  }

  public int getIncidentId() {
    return incidentId;
  }

  public String getName() {
    return name;
  }

  public int getWeight() {
    return weight;
  }

  public String getServiceAddress() {
    return serviceAddress;
  }

  public void setIncidentId(int incidentId) {
    this.incidentId = incidentId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public void setServiceAddress(String serviceAddress) {
    this.serviceAddress = serviceAddress;
  }
}
