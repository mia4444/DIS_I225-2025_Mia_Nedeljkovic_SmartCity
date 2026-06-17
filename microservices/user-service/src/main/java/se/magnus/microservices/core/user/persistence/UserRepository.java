package se.magnus.microservices.core.user.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, String> {
  Mono<UserEntity> findByProductId(int productId);
}
