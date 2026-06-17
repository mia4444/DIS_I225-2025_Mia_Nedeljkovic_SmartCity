package se.magnus.api.core.alert;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AlertService {

  Mono<Alert> createReview(Alert body);

  /**
   * Sample usage: "curl $HOST:$PORT/alert?productId=1".
   *
   * @param productId Id of the incident
   * @return the reviews of the incident
   */
  @GetMapping(
    value = "/alert",
    produces = "application/json")
  Flux<Alert> getReviews(@RequestParam(value = "productId", required = true) int productId);

  Mono<Void> deleteReviews(int productId);
}
