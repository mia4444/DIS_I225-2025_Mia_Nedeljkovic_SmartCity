package se.magnus.microservices.core.incident.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface IncidentCoreRepository extends ReactiveCrudRepository<IncidentCoreEntity, String> {
  Mono<IncidentCoreEntity> findByIncidentId(int incidentId);
}
