package se.magnus.microservices.composite.incident.services;

import static java.util.logging.Level.FINE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.incident.*;
import se.magnus.api.core.alert.Alert;
import se.magnus.api.core.device.Device;
import se.magnus.api.core.incident.Incident;
import se.magnus.util.http.ServiceUtil;

@RestController
public class IncidentCompositeServiceImpl implements IncidentCompositeService {

  private static final Logger LOG = LoggerFactory.getLogger(IncidentCompositeServiceImpl.class);
  private final ServiceUtil serviceUtil;
  private final IncidentCompositeIntegration integration;

  @Autowired
  public IncidentCompositeServiceImpl(ServiceUtil serviceUtil, IncidentCompositeIntegration integration) {
    this.serviceUtil = serviceUtil;
    this.integration = integration;
  }

  @Override
  public Mono<Void> createIncident(IncidentAggregate body) {

    try {

      List<Mono> monoList = new ArrayList<>();

      LOG.info("Will create a new composite incident for incident.id: {}", body.getIncidentId());

      Incident incident = new Incident(body.getIncidentId(), body.getName(), body.getWeight(), null);
      monoList.add(integration.createIncident(incident));

      if (body.getDevices() != null) {
        body.getDevices().forEach(r -> {
          Device device = new Device(body.getIncidentId(), r.getDeviceId(), r.getAuthor(), r.getRate(), r.getContent(), null);
          monoList.add(integration.createDevice(device));
        });
      }

      if (body.getAlerts() != null) {
        body.getAlerts().forEach(r -> {
          Alert alert = new Alert(body.getIncidentId(), r.getAlertId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
          monoList.add(integration.createAlert(alert));
        });
      }

      LOG.debug("createIncidentAggregate: composite entities created for incidentId: {}", body.getIncidentId());

      return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
        .doOnError(ex -> LOG.warn("createIncident failed: {}", ex.toString()))
        .then();

    } catch (RuntimeException re) {
      LOG.warn("createIncident failed: {}", re.toString());
      throw re;
    }
  }

  @Override
  public Mono<IncidentAggregate> getIncident(int incidentId) {

    LOG.info("Will get composite incident info for incident.id={}", incidentId);
      return Mono.zip(
      values -> createIncidentAggregate((Incident) values[0], (List<Device>) values[1], (List<Alert>) values[2], serviceUtil.getServiceAddress()),
      integration.getIncident(incidentId),
      integration.getDevices(incidentId).collectList(),
      integration.getAlerts(incidentId).collectList())
      .doOnError(ex -> LOG.warn("getIncident failed: {}", ex.toString()))
      .log(LOG.getName(), FINE);
  }

  @Override
  public Mono<Void> deleteIncident(int incidentId) {

    try {

      LOG.info("Will delete a composite incident aggregate for incident.id: {}", incidentId);

      return Mono.zip(
        r -> "",
        integration.deleteIncident(incidentId),
        integration.deleteAlerts(incidentId),
        integration.deleteDevices(incidentId))
        .doOnError(ex -> LOG.warn("deleteIncident failed: {}", ex.toString()))
        .log(LOG.getName(), FINE).then();

    } catch (RuntimeException re) {
      LOG.warn("deleteIncident failed: {}", re.toString());
      throw re;
    }
  }

  private IncidentAggregate createIncidentAggregate(Incident incident, List<Device> devices, List<Alert> alerts, String serviceAddress) {

    // 1. Setup incident info
    int incidentId = incident.getIncidentId();
    String name = incident.getName();
    int weight = incident.getWeight();

    // 2. Copy summary device info, if available
    List<DeviceSummary> deviceSummaries = (devices == null) ? null :
       devices.stream()
        .map(r -> new DeviceSummary(r.getDeviceId(), r.getAuthor(), r.getRate(), r.getContent()))
        .collect(Collectors.toList());

    // 3. Copy summary alert info, if available
    List<AlertSummary> alertSummaries = (alerts == null)  ? null :
      alerts.stream()
        .map(r -> new AlertSummary(r.getAlertId(), r.getAuthor(), r.getSubject(), r.getContent()))
        .collect(Collectors.toList());

    // 4. Create info regarding the involved microservices addresses
    String incidentAddress = incident.getServiceAddress();
    String alertAddress = (alerts != null && alerts.size() > 0) ? alerts.get(0).getServiceAddress() : "";
    String deviceAddress = (devices != null && devices.size() > 0) ? devices.get(0).getServiceAddress() : "";
    ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, incidentAddress, alertAddress, deviceAddress);

    return new IncidentAggregate(incidentId, name, weight, deviceSummaries, alertSummaries, serviceAddresses);
  }
}
