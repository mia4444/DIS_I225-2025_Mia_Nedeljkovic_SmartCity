package se.magnus.microservices.core.product;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.incident.Incident;
import se.magnus.microservices.core.product.services.UserMapper;

class MapperTests {

  private UserMapper mapper = Mappers.getMapper(UserMapper.class);

  @Test
  void mapperTests() {

    assertNotNull(mapper);

    Incident api = new Incident(1, "n", 1, "sa");

    se.magnus.microservices.core.product.persistence.UserEntity entity = mapper.apiToEntity(api);

    assertEquals(api.getProductId(), entity.getProductId());
    assertEquals(api.getProductId(), entity.getProductId());
    assertEquals(api.getName(), entity.getName());
    assertEquals(api.getWeight(), entity.getWeight());

    Incident api2 = mapper.entityToApi(entity);

    assertEquals(api.getProductId(), api2.getProductId());
    assertEquals(api.getProductId(), api2.getProductId());
    assertEquals(api.getName(),      api2.getName());
    assertEquals(api.getWeight(),    api2.getWeight());
    assertNull(api2.getServiceAddress());
  }
}
