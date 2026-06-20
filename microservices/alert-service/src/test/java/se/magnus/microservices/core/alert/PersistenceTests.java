package se.magnus.microservices.core.alert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersistenceTests extends MySqlTestBase {

  @Autowired
  private se.magnus.microservices.core.alert.persistence.AlertRepository repository;

  private se.magnus.microservices.core.alert.persistence.AlertEntity savedEntity;

  @BeforeEach
  void setupDb() {
    repository.deleteAll();

    se.magnus.microservices.core.alert.persistence.AlertEntity entity = new se.magnus.microservices.core.alert.persistence.AlertEntity(1, 2, "a", "s", "c");
    savedEntity = repository.save(entity);

    assertEqualsAlert(entity, savedEntity);
  }

  @Test
  void create() {

    se.magnus.microservices.core.alert.persistence.AlertEntity newEntity = new se.magnus.microservices.core.alert.persistence.AlertEntity(1, 3, "a", "s", "c");
    repository.save(newEntity);

    se.magnus.microservices.core.alert.persistence.AlertEntity foundEntity = repository.findById(newEntity.getId()).get();
    assertEqualsAlert(newEntity, foundEntity);

    assertEquals(2, repository.count());
  }

  @Test
  void update() {
    savedEntity.setAuthor("a2");
    repository.save(savedEntity);

    se.magnus.microservices.core.alert.persistence.AlertEntity foundEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, (long)foundEntity.getVersion());
    assertEquals("a2", foundEntity.getAuthor());
  }

  @Test
  void delete() {
    repository.delete(savedEntity);
    assertFalse(repository.existsById(savedEntity.getId()));
  }

  @Test
  void getByIncidentId() {
    List<se.magnus.microservices.core.alert.persistence.AlertEntity> entityList = repository.findByIncidentId(savedEntity.getIncidentId());

    assertThat(entityList, hasSize(1));
    assertEqualsAlert(savedEntity, entityList.get(0));
  }

  @Test
  void duplicateError() {
    assertThrows(DataIntegrityViolationException.class, () -> {
      se.magnus.microservices.core.alert.persistence.AlertEntity entity = new se.magnus.microservices.core.alert.persistence.AlertEntity(1, 2, "a", "s", "c");
      repository.save(entity);
    });
  }

  @Test
  void optimisticLockError() {

    se.magnus.microservices.core.alert.persistence.AlertEntity entity1 = repository.findById(savedEntity.getId()).get();
    se.magnus.microservices.core.alert.persistence.AlertEntity entity2 = repository.findById(savedEntity.getId()).get();

    entity1.setAuthor("a1");
    repository.save(entity1);

    assertThrows(OptimisticLockingFailureException.class, () -> {
      entity2.setAuthor("a2");
      repository.save(entity2);
    });

    se.magnus.microservices.core.alert.persistence.AlertEntity updatedEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, (int)updatedEntity.getVersion());
    assertEquals("a1", updatedEntity.getAuthor());
  }

  private void assertEqualsAlert(se.magnus.microservices.core.alert.persistence.AlertEntity expectedEntity, se.magnus.microservices.core.alert.persistence.AlertEntity actualEntity) {
    assertEquals(expectedEntity.getId(),        actualEntity.getId());
    assertEquals(expectedEntity.getVersion(),   actualEntity.getVersion());
    assertEquals(expectedEntity.getIncidentId(), actualEntity.getIncidentId());
    assertEquals(expectedEntity.getAlertId(),  actualEntity.getAlertId());
    assertEquals(expectedEntity.getAuthor(),    actualEntity.getAuthor());
    assertEquals(expectedEntity.getSubject(),   actualEntity.getSubject());
    assertEquals(expectedEntity.getContent(),   actualEntity.getContent());
  }
}
