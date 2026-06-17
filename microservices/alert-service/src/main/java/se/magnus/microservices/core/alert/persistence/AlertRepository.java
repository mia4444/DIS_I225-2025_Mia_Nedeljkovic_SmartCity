package se.magnus.microservices.core.alert.persistence;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AlertRepository extends CrudRepository<AlertEntity, Integer> {

  @Transactional(readOnly = true)
  List<AlertEntity> findByProductId(int productId);
}
