package se.magnus.microservices.core.incident.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.incident.Incident;

@Mapper(componentModel = "spring")
public interface IncidentCoreMapper {

  @Mappings({
    @Mapping(target = "serviceAddress", ignore = true)
  })
  Incident entityToApi(se.magnus.microservices.core.incident.persistence.IncidentCoreEntity entity);

  @Mappings({
    @Mapping(target = "id", ignore = true), @Mapping(target = "version", ignore = true)
  })
  se.magnus.microservices.core.incident.persistence.IncidentCoreEntity apiToEntity(Incident api);
}
