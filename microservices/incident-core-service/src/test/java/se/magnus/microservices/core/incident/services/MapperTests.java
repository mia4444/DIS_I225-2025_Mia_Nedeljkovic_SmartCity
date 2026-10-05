package se.magnus.microservices.core.user.services;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.incident.Incident;
import se.magnus.microservices.core.user.persistence.UserEntity;

class MapperTests {
    private UserMapper mapper= Mappers.getMapper(UserMapper.class);
  @Test
  void mapperTests() {

      assertNotNull(mapper);

      Incident api=new Incident(1,"Pukla vodovodna cev",1,"sa");

      UserEntity entity=mapper.apiToEntity(api);

      assertEquals(api.getIncidentId(), entity.getIncidentId());
      assertEquals(api.getName(), entity.getName());
      assertEquals(api.getWeight(), entity.getWeight());

      Incident api2=mapper.entityToApi(entity);

      assertEquals(api.getIncidentId(),api2.getIncidentId());
      assertEquals(api.getName(),api2.getName());
      assertEquals(api.getWeight(),api2.getWeight());
      assertNull(api2.getServiceAddress());
  }
}