package se.magnus.microservices.core.telemetry.services;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.telemetry.Telemetry;
import se.magnus.api.core.telemetry.TelemetryService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.microservices.core.telemetry.persistence.TelemetryEntity;
import se.magnus.microservices.core.telemetry.persistence.TelemetryRepository;
import se.magnus.util.http.ServiceUtil;

@RestController
public class TelemetryServiceImpl implements TelemetryService {

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
  public Mono<Telemetry> createReading(Telemetry body) {

    if (body.getDeviceId() < 1) {
      throw new InvalidInputException("Invalid deviceId: " + body.getDeviceId());
    }

    TelemetryEntity entity = mapper.apiToEntity(body);
    return repository.save(entity)
      .log(LOG.getName(), FINE)
      .onErrorMap(
        DuplicateKeyException.class,
        ex -> new InvalidInputException("Duplicate key, Device Id: " + body.getDeviceId()))
      .map(mapper::entityToApi);
  }

  @Override
  public Flux<Telemetry> getReadings(int deviceId) {

    if (deviceId < 1) {
      throw new InvalidInputException("Invalid deviceId: " + deviceId);
    }

    LOG.info("Will get telemetry readings for device with id={}", deviceId);

    return repository.findByDeviceId(deviceId)
      .log(LOG.getName(), FINE)
      .map(mapper::entityToApi)
      .map(this::setServiceAddress);
  }

  @Override
  public Mono<Void> deleteReadings(int deviceId) {

    if (deviceId < 1) {
      throw new InvalidInputException("Invalid deviceId: " + deviceId);
    }
    return repository.deleteAll(repository.findByDeviceId(deviceId));
  }

  private Telemetry setServiceAddress(Telemetry e) {
    e.setServiceAddress(serviceUtil.getServiceAddress());
    return e;
  }
}
