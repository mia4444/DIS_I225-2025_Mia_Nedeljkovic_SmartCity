package se.magnus.microservices.core.incident.services;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.core.incident.Incident;
import se.magnus.api.core.incident.IncidentService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;
import se.magnus.microservices.core.incident.persistence.IncidentCoreEntity;
import se.magnus.microservices.core.incident.persistence.IncidentCoreRepository;

@RestController
public class IncidentCoreServiceImpl implements IncidentService {

  private static final Logger LOG = LoggerFactory.getLogger(IncidentCoreServiceImpl.class);

  private final ServiceUtil serviceUtil;

  private final IncidentCoreRepository repository;

  private final IncidentCoreMapper mapper;

  @Autowired
  public IncidentCoreServiceImpl(IncidentCoreRepository repository, IncidentCoreMapper mapper, ServiceUtil serviceUtil) {
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
  }

  @Override
  public Mono<Incident> createIncident(Incident body) {

    if (body.getIncidentId() < 1) {
      throw new InvalidInputException("Invalid incidentId: " + body.getIncidentId());
    }

    IncidentCoreEntity entity = mapper.apiToEntity(body);
    Mono<Incident> newEntity = repository.save(entity)
      .log(LOG.getName(), FINE)
      .onErrorMap(
        DuplicateKeyException.class,
        ex -> new InvalidInputException("Duplicate key, Incident Id: " + body.getIncidentId()))
      .map(e -> mapper.entityToApi(e));

    return newEntity;
  }

  @Override
  public Mono<Incident> getIncident(int incidentId) {

    if (incidentId < 1) {
      throw new InvalidInputException("Invalid incidentId: " + incidentId);
    }

    LOG.info("Will get incident info for id={}", incidentId);

    return repository.findByIncidentId(incidentId)
      .switchIfEmpty(Mono.error(new NotFoundException("No incident found for incidentId: " + incidentId)))
      .log(LOG.getName(), FINE)
      .map(e -> mapper.entityToApi(e))
      .map(e -> setServiceAddress(e));
  }

  @Override
  public Mono<Void> deleteIncident(int incidentId) {

    if (incidentId < 1) {
      throw new InvalidInputException("Invalid incidentId: " + incidentId);
    }

    LOG.debug("deleteIncident: tries to delete an entity with incidentId: {}", incidentId);
    return repository.findByIncidentId(incidentId).log(LOG.getName(), FINE).map(e -> repository.delete(e)).flatMap(e -> e);
  }

  private Incident setServiceAddress(Incident e) {
    e.setServiceAddress(serviceUtil.getServiceAddress());
    return e;
  }
}