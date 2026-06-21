package se.magnus.api.composite.incident;

import java.util.List;

public class IncidentAggregate {
  private final int incidentId;
  private final String name;
  private final int weight;
  private final List<DeviceSummary> devices;
  private final List<AlertSummary> alerts;
  private final ServiceAddresses serviceAddresses;

  public IncidentAggregate() {
    incidentId = 0;
    name = null;
    weight = 0;
    devices = null;
    alerts = null;
    serviceAddresses = null;
  }

  public IncidentAggregate(
    int incidentId,
    String name,
    int weight,
    List<DeviceSummary> devices,
    List<AlertSummary> alerts,
    ServiceAddresses serviceAddresses) {

    this.incidentId = incidentId;
    this.name = name;
    this.weight = weight;
    this.devices = devices;
    this.alerts = alerts;
    this.serviceAddresses = serviceAddresses;
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

  public List<DeviceSummary> getDevices() {
    return devices;
  }

  public List<AlertSummary> getAlerts() {
    return alerts;
  }

  public ServiceAddresses getServiceAddresses() {
    return serviceAddresses;
  }
}
