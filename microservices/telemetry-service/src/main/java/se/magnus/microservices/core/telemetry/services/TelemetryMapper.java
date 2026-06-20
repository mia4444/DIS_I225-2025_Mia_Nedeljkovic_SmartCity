package se.magnus.microservices.core.telemetry.services;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.telemetry.Telemetry;
import se.magnus.microservices.core.telemetry.persistence.TelemetryEntity;

@Mapper(componentModel = "spring")
public interface TelemetryMapper {

  @Mappings({
    @Mapping(target = "serviceAddress", ignore = true)
  })
  Telemetry entityToApi(TelemetryEntity entity);

  @Mappings({
    @Mapping(target = "id", ignore = true),
    @Mapping(target = "version", ignore = true)
  })
  TelemetryEntity apiToEntity(Telemetry api);

  List<Telemetry> entityListToApiList(List<TelemetryEntity> entity);

  List<TelemetryEntity> apiListToEntityList(List<Telemetry> api);
}