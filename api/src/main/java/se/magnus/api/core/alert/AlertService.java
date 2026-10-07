package se.magnus.api.core.alert;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AlertService {

  Mono<Alert> createAlert(Alert body);

  /**
   * Sample usage: "curl $HOST:$PORT/alert?incidentId=1".
   *
   * @param incidentId Id of the incident
   * @return the alerts of the incident
   */
  @GetMapping(
    value = "/alert",
    produces = "application/json")
  Flux<Alert> getAlerts(@RequestParam(value = "incidentId", required = true) int incidentId);

  Mono<Void> deleteAlerts(int incidentId);
}
