package se.magnus.api.core.incident;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface IncidentService {

  Mono<Incident> createProduct(Incident body);

  /**
   * Sample usage: "curl $HOST:$PORT/incident/1".
   *
   * @param productId Id of the incident
   * @return the incident, if found, else null
   */
  @GetMapping(
    value = "/incident/{productId}",
    produces = "application/json")
  Mono<Incident> getProduct(@PathVariable int productId);

  Mono<Void> deleteProduct(int productId);
}
