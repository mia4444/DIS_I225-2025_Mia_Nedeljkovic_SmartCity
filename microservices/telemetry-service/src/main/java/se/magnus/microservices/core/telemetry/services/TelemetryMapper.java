package se.magnus.microservices.core.telemetry.services;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.device.Device;

@Mapper(componentModel = "spring")
public interface TelemetryMapper {

  @Mappings({
    @Mapping(target = "rate", source = "entity.rating"),
    @Mapping(target = "serviceAddress", ignore = true)
  })
  Device entityToApi(se.magnus.microservices.core.telemetry.persistence.TelemetryEntity entity);

  @Mappings({
    @Mapping(target = "rating", source = "api.rate"),
    @Mapping(target = "id", ignore = true),
    @Mapping(target = "version", ignore = true)
  })
  se.magnus.microservices.core.telemetry.persistence.TelemetryEntity apiToEntity(Device api);

  List<Device> entityListToApiList(List<se.magnus.microservices.core.telemetry.persistence.TelemetryEntity> entity);

  List<se.magnus.microservices.core.telemetry.persistence.TelemetryEntity> apiListToEntityList(List<Device> api);
}