package se.magnus.microservices.core.alert.services;

import static java.util.logging.Level.FINE;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.alert.Alert;
import se.magnus.api.core.alert.AlertService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;
import se.magnus.microservices.core.alert.persistence.AlertRepository;
import se.magnus.microservices.core.alert.persistence.AlertEntity;

@RestController
public class AlertServiceImpl implements AlertService {

  private static final Logger LOG = LoggerFactory.getLogger(AlertServiceImpl.class);

  private final AlertRepository repository;

  private final AlertMapper mapper;

  private final ServiceUtil serviceUtil;

  private final Scheduler jdbcScheduler;

  @Autowired
  public AlertServiceImpl(@Qualifier("jdbcScheduler") Scheduler jdbcScheduler, AlertRepository repository, AlertMapper mapper, ServiceUtil serviceUtil) {
    this.jdbcScheduler = jdbcScheduler;
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
  }

  @Override
  public Mono<Alert> createAlert(Alert body) {

    if (body.getIncidentId() < 1) {
      throw new InvalidInputException("Invalid incidentId: " + body.getIncidentId());
    }
    return Mono.fromCallable(() -> internalCreateAlert(body))
      .subscribeOn(jdbcScheduler);
  }

  private Alert internalCreateAlert(Alert body) {
    try {
      AlertEntity entity = mapper.apiToEntity(body);
      AlertEntity newEntity = repository.save(entity);

      LOG.debug("createAlert: created a alert entity: {}/{}", body.getIncidentId(), body.getAlertId());
      return mapper.entityToApi(newEntity);

    } catch (DataIntegrityViolationException dive) {
      throw new InvalidInputException("Duplicate key, Incident Id: " + body.getIncidentId() + ", Alert Id:" + body.getAlertId());
    }
  }

  @Override
  public Flux<Alert> getAlerts(int incidentId) {

    if (incidentId < 1) {
      throw new InvalidInputException("Invalid incidentId: " + incidentId);
    }

    LOG.info("Will get alerts for incident with id={}", incidentId);

    return Mono.fromCallable(() -> internalGetAlerts(incidentId))
      .flatMapMany(Flux::fromIterable)
      .log(LOG.getName(), FINE)
      .subscribeOn(jdbcScheduler);
  }

  private List<Alert> internalGetAlerts(int incidentId) {

    List<AlertEntity> entityList = repository.findByIncidentId(incidentId);
    List<Alert> list = mapper.entityListToApiList(entityList);
    list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

    LOG.debug("Response size: {}", list.size());

    return list;
  }

  @Override
  public Mono<Void> deleteAlerts(int incidentId) {

    if (incidentId < 1) {
      throw new InvalidInputException("Invalid incidentId: " + incidentId);
    }

    return Mono.fromRunnable(() -> internalDeleteAlerts(incidentId)).subscribeOn(jdbcScheduler).then();
  }

  private void internalDeleteAlerts(int incidentId) {

    LOG.debug("deleteAlerts: tries to delete alerts for the incident with incidentId: {}", incidentId);

    repository.deleteAll(repository.findByIncidentId(incidentId));
  }
}