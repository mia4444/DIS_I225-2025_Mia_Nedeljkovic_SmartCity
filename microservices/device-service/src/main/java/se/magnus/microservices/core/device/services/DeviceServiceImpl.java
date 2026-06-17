package se.magnus.microservices.core.device.services;

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
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.core.device.persistence.DeviceEntity;
import se.magnus.microservices.core.device.persistence.DeviceRepository;
import se.magnus.util.http.ServiceUtil;

@RestController
public class DeviceServiceImpl implements DeviceService {

  private static final Logger LOG = LoggerFactory.getLogger(DeviceServiceImpl.class);

  private final ServiceUtil serviceUtil;

  private final DeviceRepository repository;

  private final DeviceMapper mapper;

  @Autowired
  public DeviceServiceImpl(DeviceRepository repository, DeviceMapper mapper, ServiceUtil serviceUtil) {
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
  }

  @Override
  public Mono<Device> createRecommendation(Device body) {

    if (body.getProductId() < 1) {
      throw new InvalidInputException("Invalid productId: " + body.getProductId());
    }

    DeviceEntity entity = mapper.apiToEntity(body);
    Mono<Device> newEntity = repository.save(entity)
            .log(LOG.getName(), FINE)
            .onErrorMap(
                    DuplicateKeyException.class,
                    ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
            .map(e -> mapper.entityToApi(e));

    return newEntity;
  }

  @Override
  public Flux<Device> getRecommendations(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    LOG.info("Will get devices for productId={}", productId);

    return repository.findByProductId(productId)
            .log(LOG.getName(), FINE)
            .map(e -> mapper.entityToApi(e))
            .map(this::setServiceAddress);
  }

  @Override
  public Mono<Void> deleteRecommendations(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    LOG.debug("deleteRecommendations: tries to delete devices for productId: {}", productId);
    return repository.deleteAll(repository.findByProductId(productId));
  }

  private Device setServiceAddress(Device e) {
    e.setServiceAddress(serviceUtil.getServiceAddress());
    return e;
  }
}