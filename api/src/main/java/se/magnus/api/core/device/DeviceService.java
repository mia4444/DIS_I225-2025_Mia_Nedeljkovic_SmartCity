package se.magnus.api.core.device;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviceService {

  Mono<Device> createDevice(Device body);

  /**
   * Sample usage: "curl $HOST:$PORT/device?incidentId=1".
   *
   * @param incidentId Id of the incident
   * @return the devices linked to the incident
   */
  @GetMapping(
    value = "/device",
    produces = "application/json")
  Flux<Device> getDevices(
    @RequestParam(value = "incidentId", required = true) int incidentId);

  Mono<Void> deleteDevices(int incidentId);
}
