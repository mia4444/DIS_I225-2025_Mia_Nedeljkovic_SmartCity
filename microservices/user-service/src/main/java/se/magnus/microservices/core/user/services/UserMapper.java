package se.magnus.microservices.core.user.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.incident.Incident;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mappings({
    @Mapping(target = "serviceAddress", ignore = true)
  })
  Incident entityToApi(se.magnus.microservices.core.user.persistence.UserEntity entity);

  @Mappings({
    @Mapping(target = "id", ignore = true), @Mapping(target = "version", ignore = true)
  })
  se.magnus.microservices.core.user.persistence.UserEntity apiToEntity(Incident api);
}
