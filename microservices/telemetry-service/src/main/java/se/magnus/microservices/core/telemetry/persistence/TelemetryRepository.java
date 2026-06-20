package se.magnus.microservices.core.telemetry.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TelemetryRepository extends ReactiveCrudRepository<TelemetryEntity, String> {
  Flux<TelemetryEntity> findByDeviceId(int deviceId);
}
