package se.magnus.microservices.core.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;
import se.magnus.microservices.core.user.persistence.UserEntity;
import se.magnus.microservices.core.user.persistence.UserRepository;
import org.springframework.dao.DuplicateKeyException;

@DataMongoTest
class PersistenceTests extends MongoDbTestBase{
    @Autowired
    private UserRepository repository;
    private UserEntity savedEntity;

    @BeforeEach
    void setupDb(){
        StepVerifier.create(repository.deleteAll()).verifyComplete();

        UserEntity entity = new UserEntity(1, "Pukla vodovodna cev", 1);
        StepVerifier.create(repository.save(entity))
                .expectNextMatches(createdEntity ->{
                    savedEntity = createdEntity;
                    return areUserEntitiesEqual(entity, savedEntity);
                })
                .verifyComplete();
    }

    @Test
    void create(){
        UserEntity newEntity=new UserEntity (2,"Kvar na semaforu",1);

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
        UserEntity entity=new UserEntity(savedEntity.getIncidentId(), "Duplikat",1);
        StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
    }

    @Test
    void optimisticLockError(){
        UserEntity entity1=repository.findById(savedEntity.getId()).block();
        UserEntity entity2=repository.findById(savedEntity.getId()).block();

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


    private boolean areUserEntitiesEqual(UserEntity expectedEntity, UserEntity actualEntity){
        return
           (expectedEntity.getId().equals(actualEntity.getId()))
           &&(expectedEntity.getVersion()== actualEntity.getVersion())
           &&(expectedEntity.getIncidentId()== actualEntity.getIncidentId())
           &&(expectedEntity.getName().equals(actualEntity.getName()))
                   &&(expectedEntity.getWeight()== actualEntity.getWeight())     ;

    }
}