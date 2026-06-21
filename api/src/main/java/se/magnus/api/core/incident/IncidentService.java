package se.magnus.api.core.incident;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface IncidentService {

  Mono<Incident> createIncident(Incident body);

  /**
   * Sample usage: "curl $HOST:$PORT/incident/1".
   *
   * @param incidentId Id of the incident
   * @return the incident, if found, else null
   */

  @GetMapping(
    value = "/incident/{incidentId}",
    produces = "application/json")
  Mono<Incident> getIncident(@PathVariable int incidentId);

  Mono<Void> deleteIncident(int incidentId);
}
