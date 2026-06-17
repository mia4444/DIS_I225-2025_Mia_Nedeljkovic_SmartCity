package se.magnus.microservices.composite.product.services;

import static java.util.logging.Level.FINE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.incident.*;
import se.magnus.api.core.alert.Alert;
import se.magnus.api.core.device.Device;
import se.magnus.api.core.incident.Incident;
import se.magnus.util.http.ServiceUtil;

@RestController
public class IncidentCompositeServiceImpl implements IncidentCompositeService {

  private static final Logger LOG = LoggerFactory.getLogger(se.magnus.microservices.composite.product.services.IncidentCompositeServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private final ProductCompositeIntegration integration;

  @Autowired
  public IncidentCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
    this.serviceUtil = serviceUtil;
    this.integration = integration;
  }

  @Override
  public Mono<Void> createProduct(IncidentAggregate body) {

    try {

      List<Mono> monoList = new ArrayList<>();

      LOG.info("Will create a new composite entity for incident.id: {}", body.getProductId());

      Incident incident = new Incident(body.getProductId(), body.getName(), body.getWeight(), null);
      monoList.add(integration.createProduct(incident));

      if (body.getRecommendations() != null) {
        body.getRecommendations().forEach(r -> {
          Device device = new Device(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
          monoList.add(integration.createRecommendation(device));
        });
      }

      if (body.getReviews() != null) {
        body.getReviews().forEach(r -> {
          Alert alert = new Alert(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
          monoList.add(integration.createReview(alert));
        });
      }

      LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

      return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
        .doOnError(ex -> LOG.warn("createCompositeProduct failed: {}", ex.toString()))
        .then();

    } catch (RuntimeException re) {
      LOG.warn("createCompositeProduct failed: {}", re.toString());
      throw re;
    }
  }

  @Override
  public Mono<IncidentAggregate> getProduct(int productId) {

    LOG.info("Will get composite incident info for incident.id={}", productId);
    return Mono.zip(
      values -> createProductAggregate((Incident) values[0], (List<Device>) values[1], (List<Alert>) values[2], serviceUtil.getServiceAddress()),
      integration.getProduct(productId),
      integration.getRecommendations(productId).collectList(),
      integration.getReviews(productId).collectList())
      .doOnError(ex -> LOG.warn("getCompositeProduct failed: {}", ex.toString()))
      .log(LOG.getName(), FINE);
  }

  @Override
  public Mono<Void> deleteProduct(int productId) {

    try {

      LOG.info("Will delete a incident aggregate for incident.id: {}", productId);

      return Mono.zip(
        r -> "",
        integration.deleteProduct(productId),
        integration.deleteRecommendations(productId),
        integration.deleteReviews(productId))
        .doOnError(ex -> LOG.warn("delete failed: {}", ex.toString()))
        .log(LOG.getName(), FINE).then();

    } catch (RuntimeException re) {
      LOG.warn("deleteCompositeProduct failed: {}", re.toString());
      throw re;
    }
  }

  private IncidentAggregate createProductAggregate(Incident incident, List<Device> recommendations, List<Alert> reviews, String serviceAddress) {

    // 1. Setup incident info
    int productId = incident.getProductId();
    String name = incident.getName();
    int weight = incident.getWeight();

    // 2. Copy summary device info, if available
    List<DeviceSummary> recommendationSummaries = (recommendations == null) ? null :
       recommendations.stream()
        .map(r -> new DeviceSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
        .collect(Collectors.toList());

    // 3. Copy summary alert info, if available
    List<AlertSummary> reviewSummaries = (reviews == null)  ? null :
      reviews.stream()
        .map(r -> new AlertSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
        .collect(Collectors.toList());

    // 4. Create info regarding the involved microservices addresses
    String productAddress = incident.getServiceAddress();
    String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
    String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
    ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

    return new IncidentAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
  }
}
