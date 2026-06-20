package se.magnus.microservices.core.device.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.device.Device;
import se.magnus.microservices.core.device.persistence.DeviceEntity;

class MapperTests {

  private DeviceMapper mapper = Mappers.getMapper(DeviceMapper.class);

  @Test
  void mapperTests() {

    assertNotNull(mapper);

    Device api = new Device(1, 2, "author", 3, "content", "sa");

    DeviceEntity entity = mapper.apiToEntity(api);

    assertEquals(api.getIncidentId(), entity.getIncidentId());
    assertEquals(api.getDeviceId(), entity.getDeviceId());
    assertEquals(api.getAuthor(), entity.getAuthor());
    assertEquals(api.getRate(), entity.getRate());
    assertEquals(api.getContent(), entity.getContent());

    Device api2 = mapper.entityToApi(entity);

    assertEquals(api.getIncidentId(), api2.getIncidentId());
    assertEquals(api.getDeviceId(), api2.getDeviceId());
    assertEquals(api.getAuthor(), api2.getAuthor());
    assertEquals(api.getRate(), api2.getRate());
    assertEquals(api.getContent(), api2.getContent());
    assertNull(api2.getServiceAddress());
  }
}
