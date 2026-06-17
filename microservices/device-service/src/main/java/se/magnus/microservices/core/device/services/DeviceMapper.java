package se.magnus.microservices.core.device.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.device.Device;
import se.magnus.microservices.core.device.persistence.DeviceEntity;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

  @Mappings({
          @Mapping(target = "serviceAddress", ignore = true)
  })
  Device entityToApi(DeviceEntity entity);

  @Mappings({
          @Mapping(target = "id", ignore = true), @Mapping(target = "version", ignore = true)
  })
  DeviceEntity apiToEntity(Device api);
}