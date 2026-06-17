package se.magnus.microservices.core.telemetry.services;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.device.Device;
import se.magnus.api.core.device.DeviceService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.microservices.core.telemetry.persistence.TelemetryEntity;
import se.magnus.microservices.core.telemetry.persistence.TelemetryRepository;
import se.magnus.util.http.ServiceUtil;

@RestController
public class TelemetryServiceImpl implements DeviceService {

  private static final Logger LOG = LoggerFactory.getLogger(TelemetryServiceImpl.class);

  private final TelemetryRepository repository;

  private final TelemetryMapper mapper;

  private final ServiceUtil serviceUtil;

  @Autowired
  public TelemetryServiceImpl(TelemetryRepository repository, TelemetryMapper mapper, ServiceUtil serviceUtil) {
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
  }

  @Override
  public Mono<Device> createRecommendation(Device body) {

    if (body.getProductId() < 1) {
      throw new InvalidInputException("Invalid productId: " + body.getProductId());
    }

    TelemetryEntity entity = mapper.apiToEntity(body);
    Mono<Device> newEntity = repository.save(entity)
      .log(LOG.getName(), FINE)
      .onErrorMap(
        DuplicateKeyException.class,
        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
      .map(e -> mapper.entityToApi(e));

    return newEntity;
  }

  @Override
  public Flux<Device> getRecommendations(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    LOG.info("Will get recommendations for incident with id={}", productId);

    return repository.findByProductId(productId)
      .log(LOG.getName(), FINE)
      .map(e -> mapper.entityToApi(e))
      .map(e -> setServiceAddress(e));
  }

  @Override
  public Mono<Void> deleteRecommendations(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    LOG.debug("deleteRecommendations: tries to delete recommendations for the incident with productId: {}", productId);
    return repository.deleteAll(repository.findByProductId(productId));
  }

  private Device setServiceAddress(Device e) {
    e.setServiceAddress(serviceUtil.getServiceAddress());
    return e;
  }
}
