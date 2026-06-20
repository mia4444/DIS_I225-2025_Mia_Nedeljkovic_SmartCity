package se.magnus.microservices.core.device;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;
import se.magnus.microservices.core.device.persistence.DeviceEntity;
import se.magnus.microservices.core.device.persistence.DeviceRepository;

@DataMongoTest
class PersistenceTests extends MongoDbTestBase {

  @Autowired
  private DeviceRepository repository;

  private DeviceEntity savedEntity;

  @BeforeEach
  void setupDb() {
    StepVerifier.create(repository.deleteAll()).verifyComplete();

    DeviceEntity entity = new DeviceEntity(1, 1, "author", 2, "content");
    StepVerifier.create(repository.save(entity))
      .expectNextMatches(createdEntity -> {
        savedEntity = createdEntity;
        return areDeviceEqual(entity, savedEntity);
      })
      .verifyComplete();
  }

  @Test
  void create() {
    DeviceEntity newEntity = new DeviceEntity(1, 2, "author2", 3, "content2");

    StepVerifier.create(repository.save(newEntity))
      .expectNextMatches(createdEntity -> newEntity.getDeviceId() == createdEntity.getDeviceId())
      .verifyComplete();

    StepVerifier.create(repository.findById(newEntity.getId()))
      .expectNextMatches(foundEntity -> areDeviceEqual(newEntity, foundEntity))
      .verifyComplete();

    StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();
  }

  @Test
  void update() {
    savedEntity.setContent("updated");
    StepVerifier.create(repository.save(savedEntity))
      .expectNextMatches(updatedEntity -> updatedEntity.getContent().equals("updated"))
      .verifyComplete();

    StepVerifier.create(repository.findById(savedEntity.getId()))
      .expectNextMatches(foundEntity ->
        foundEntity.getVersion() == 1
        && foundEntity.getContent().equals("updated"))
      .verifyComplete();
  }

  @Test
  void delete() {
    StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
    StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
  }

  @Test
  void getByIncidentId() {
    StepVerifier.create(repository.findByIncidentId(savedEntity.getIncidentId()))
      .expectNextMatches(foundEntity -> areDeviceEqual(savedEntity, foundEntity))
      .verifyComplete();
  }

  @Test
  void optimisticLockError() {
    DeviceEntity entity1 = repository.findById(savedEntity.getId()).block();
    DeviceEntity entity2 = repository.findById(savedEntity.getId()).block();

    entity1.setContent("first");
    repository.save(entity1).block();

    StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();

    StepVerifier.create(repository.findById(savedEntity.getId()))
      .expectNextMatches(foundEntity ->
        foundEntity.getVersion() == 1
        && foundEntity.getContent().equals("first"))
      .verifyComplete();
  }

  private boolean areDeviceEqual(DeviceEntity expectedEntity, DeviceEntity actualEntity) {
    return
      expectedEntity.getId().equals(actualEntity.getId())
      && expectedEntity.getVersion() == actualEntity.getVersion()
      && expectedEntity.getIncidentId() == actualEntity.getIncidentId()
      && expectedEntity.getDeviceId() == actualEntity.getDeviceId()
      && expectedEntity.getAuthor().equals(actualEntity.getAuthor())
      && expectedEntity.getRate() == actualEntity.getRate()
      && expectedEntity.getContent().equals(actualEntity.getContent());
  }
}
