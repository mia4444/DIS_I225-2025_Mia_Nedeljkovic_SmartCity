package se.magnus.microservices.core.device.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface DeviceRepository extends ReactiveCrudRepository<DeviceEntity, String> {
  Flux<DeviceEntity> findByProductId(int productId);
}