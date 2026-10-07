package se.magnus.api.composite.incident;

public class ServiceAddresses {
  private final String compositeAddress;
  private final String incidentAddress;
  private final String alertAddress;
  private final String deviceAddress;

  public ServiceAddresses() {
    compositeAddress = null;
    incidentAddress = null;
    alertAddress = null;
    deviceAddress = null;
  }

  public ServiceAddresses(
    String compositeAddress,
    String incidentAddress,
    String alertAddress,
    String deviceAddress) {

    this.compositeAddress = compositeAddress;
    this.incidentAddress = incidentAddress;
    this.alertAddress = alertAddress;
    this.deviceAddress = deviceAddress;
  }

  public String getCompositeAddress() {
    return compositeAddress;
  }

  public String getIncidentAddress() {
    return incidentAddress;
  }

  public String getAlertAddress() {
    return alertAddress;
  }

  public String getDeviceAddress() {
    return deviceAddress;
  }
}
