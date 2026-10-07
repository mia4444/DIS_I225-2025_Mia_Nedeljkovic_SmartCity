package se.magnus.microservices.core.incident;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;
import se.magnus.microservices.core.incident.persistence.IncidentCoreEntity;
import se.magnus.microservices.core.incident.persistence.IncidentCoreRepository;
import org.springframework.dao.DuplicateKeyException;

@DataMongoTest
@Tag("docker")
class PersistenceTests extends MongoDbTestBase{
    @Autowired
    private IncidentCoreRepository repository;
    private IncidentCoreEntity savedEntity;

    @BeforeEach
    void setupDb(){
        StepVerifier.create(repository.deleteAll()).verifyComplete();

        IncidentCoreEntity entity = new IncidentCoreEntity(1, "Pukla vodovodna cev", 1);
        StepVerifier.create(repository.save(entity))
                .expectNextMatches(createdEntity ->{
                    savedEntity = createdEntity;
                    return areUserEntitiesEqual(entity, savedEntity);
                })
                .verifyComplete();
    }

    @Test
    void create(){
        IncidentCoreEntity newEntity=new IncidentCoreEntity (2,"Kvar na semaforu",1);

        StepVerifier.create(repository.save(newEntity))
                .expectNextMatches(createdEntity->newEntity.getIncidentId()==createdEntity.getIncidentId())
                .verifyComplete();

        StepVerifier.create(repository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity->areUserEntitiesEqual(newEntity,foundEntity))
                .verifyComplete();

        StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();
    }

    @Test
    void update(){
        savedEntity.setName("Naziv promenjen");
        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(updatedEntity->updatedEntity.getName().equals("Naziv promenjen"))
                .verifyComplete();
    }

    @Test
    void delete(){
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    }

    @Test
    void getByIncidentId(){
        StepVerifier.create(repository.findByIncidentId(savedEntity.getIncidentId()))
                .expectNextMatches(foundEntity->areUserEntitiesEqual(foundEntity,savedEntity)).verifyComplete();
    }

    @Test
    void duplicateError(){
        IncidentCoreEntity entity=new IncidentCoreEntity(savedEntity.getIncidentId(), "Duplikat",1);
        StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
    }

    @Test
    void optimisticLockError(){
        IncidentCoreEntity entity1=repository.findById(savedEntity.getId()).block();
        IncidentCoreEntity entity2=repository.findById(savedEntity.getId()).block();

        entity1.setName("Naziv 1");
        repository.save(entity1).block();

        StepVerifier.create(repository.save(entity2))
                .expectError(OptimisticLockingFailureException.class)
                .verify();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity->
                        foundEntity.getVersion()==1
                && foundEntity.getName().equals("Naziv 1")).verifyComplete();
    }


    private boolean areUserEntitiesEqual(IncidentCoreEntity expectedEntity, IncidentCoreEntity actualEntity){
        return
           (expectedEntity.getId().equals(actualEntity.getId()))
           &&(expectedEntity.getVersion()== actualEntity.getVersion())
           &&(expectedEntity.getIncidentId()== actualEntity.getIncidentId())
           &&(expectedEntity.getName().equals(actualEntity.getName()))
                   &&(expectedEntity.getWeight()== actualEntity.getWeight())     ;

    }
}