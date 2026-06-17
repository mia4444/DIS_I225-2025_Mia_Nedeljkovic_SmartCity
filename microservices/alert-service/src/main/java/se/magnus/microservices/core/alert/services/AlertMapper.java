package se.magnus.microservices.core.alert.services;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.alert.Alert;
import se.magnus.microservices.core.alert.persistence.AlertEntity;

@Mapper(componentModel = "spring")
public interface AlertMapper {

  @Mappings({
    @Mapping(target = "serviceAddress", ignore = true)
  })
  Alert entityToApi(AlertEntity entity);

  @Mappings({
    @Mapping(target = "id", ignore = true),
    @Mapping(target = "version", ignore = true)
  })
  AlertEntity apiToEntity(Alert api);

  List<Alert> entityListToApiList(List<AlertEntity> entity);

  List<AlertEntity> apiListToEntityList(List<Alert> api);
}