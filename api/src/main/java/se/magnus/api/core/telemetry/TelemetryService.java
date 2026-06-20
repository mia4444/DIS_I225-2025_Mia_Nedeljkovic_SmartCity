package se.magnus.api.core.telemetry;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TelemetryService {

    @PostMapping(
            value="/telemetry",
            consumes="application/json",
            produces="application/json")
    Mono <Telemetry> createReading(@RequestBody Telemetry body);

    @GetMapping(
            value="/telemetry",
            produces="application/json")
    Flux<Telemetry> getReadings(@RequestParam(value="deviceId", required=true)int deviceId);

    @DeleteMapping(value="/telemetry")
    Mono <Void> deleteReadings(@RequestParam(value="deviceId", required=true) int deviceId);

}