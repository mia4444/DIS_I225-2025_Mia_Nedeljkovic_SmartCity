package se.magnus.api.core.device;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviceService {

  Mono<Device> createRecommendation(Device body);

  /**
   * Sample usage: "curl $HOST:$PORT/device?productId=1".
   *
   * @param productId Id of the incident
   * @return the recommendations of the incident
   */
  @GetMapping(
    value = "/device",
    produces = "application/json")
  Flux<Device> getRecommendations(
    @RequestParam(value = "productId", required = true) int productId);

  Mono<Void> deleteRecommendations(int productId);
}
