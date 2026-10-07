package se.magnus.microservices.composite.incident.services;

import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Flux.empty;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.device.Device;
import se.magnus.api.core.incident.Incident;
import se.magnus.api.core.incident.IncidentService;
import se.magnus.api.core.device.DeviceService;
import se.magnus.api.core.alert.Alert;
import se.magnus.api.core.alert.AlertService;
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

@Component
public class IncidentCompositeIntegration implements IncidentService, DeviceService, AlertService {

  private static final Logger LOG = LoggerFactory.getLogger(IncidentCompositeIntegration.class);

  private static final String INCIDENT_SERVICE_URL = "http://incident-core";
  private static final String DEVICE_SERVICE_URL = "http://device";
  private static final String ALERT_SERVICE_URL = "http://alert";

  private final Scheduler publishEventScheduler;
  private final WebClient webClient;
  private final ObjectMapper mapper;
  private final StreamBridge streamBridge;

  @Autowired
  public IncidentCompositeIntegration(
    @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
    WebClient.Builder webClientBuilder,
    ObjectMapper mapper,
    StreamBridge streamBridge
  ) {
    this.webClient = webClientBuilder.build();

    this.publishEventScheduler = publishEventScheduler;
    this.mapper = mapper;
    this.streamBridge = streamBridge;
  }

  @Override
  public Mono<Incident> createIncident(Incident body) {

    return Mono.fromCallable(() -> {
      sendMessage("incidents-out-0", new Event(CREATE, body.getIncidentId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  @Override
  @CircuitBreaker(name = "incidentService", fallbackMethod = "getIncidentFallback")
  public Mono<Incident> getIncident(int incidentId) {
    String url = INCIDENT_SERVICE_URL + "/incident/" + incidentId;
    LOG.debug("Will call the getIncident API on URL: {}", url);

    return webClient.get().uri(url)
      .retrieve()
      .bodyToMono(Incident.class)
      .timeout(Duration.ofSeconds(3))
      .log(LOG.getName(), FINE)
      .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
  }

  @Override
  public Mono<Void> deleteIncident(int incidentId) {

    return Mono.fromRunnable(() -> sendMessage("incidents-out-0", new Event(DELETE, incidentId, null)))
      .subscribeOn(publishEventScheduler).then();
  }

  @Override
  public Mono<Device> createDevice(Device body) {

    return Mono.fromCallable(() -> {
      sendMessage("devices-out-0", new Event(CREATE, body.getIncidentId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  @Override
  @CircuitBreaker(name = "incidentService", fallbackMethod = "getDevicesFallback")
  public Flux<Device> getDevices(int incidentId) {

    String url = DEVICE_SERVICE_URL + "/device?incidentId=" + incidentId;

    LOG.debug("Will call the getDevices API on URL: {}", url);

    // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
    return webClient.get().uri(url)
      .retrieve()
      .bodyToFlux(Device.class)
      .timeout(Duration.ofSeconds(3))
      .log(LOG.getName(), FINE)
      .onErrorResume(error -> empty());
  }

  @Override
  public Mono<Void> deleteDevices(int incidentId) {

    return Mono.fromRunnable(() -> sendMessage("devices-out-0", new Event(DELETE, incidentId, null)))
      .subscribeOn(publishEventScheduler).then();
  }

  @Override
  public Mono<Alert> createAlert(Alert body) {

    return Mono.fromCallable(() -> {
      sendMessage("alerts-out-0", new Event(CREATE, body.getIncidentId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  @Override
  @CircuitBreaker(name = "incidentService", fallbackMethod = "getAlertsFallback")
  public Flux<Alert> getAlerts(int incidentId) {

    String url = ALERT_SERVICE_URL + "/alert?incidentId=" + incidentId;

    LOG.debug("Will call the getAlerts API on URL: {}", url);

    // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
    return webClient.get().uri(url)
      .retrieve()
      .bodyToFlux(Alert.class)
      .timeout(Duration.ofSeconds(3))
      .log(LOG.getName(), FINE)
      .onErrorResume(error -> empty());
  }

  @Override
  public Mono<Void> deleteAlerts(int incidentId) {

    return Mono.fromRunnable(() -> sendMessage("alerts-out-0", new Event(DELETE, incidentId, null)))
      .subscribeOn(publishEventScheduler).then();
  }

  private void sendMessage(String bindingName, Event event) {
    LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
    Message message = MessageBuilder.withPayload(event)
      .setHeader("partitionKey", event.getKey())
      .build();
    streamBridge.send(bindingName, message);
  }

  private Mono<Incident> getIncidentFallback(int incidentId, Throwable ex) {
    LOG.warn("Circuit breaker fallback for getIncident({}): {}", incidentId, ex.toString());
    return Mono.just(new Incident(incidentId, "INCIDENT SERVICE UNAVAILABLE", 0, "circuit-breaker-fallback"));
  }

  private Flux<Device> getDevicesFallback(int incidentId, Throwable ex) {
    LOG.warn("Circuit breaker fallback for getDevices({}): {}", incidentId, ex.toString());
    return empty();
  }

  private Flux<Alert> getAlertsFallback(int incidentId, Throwable ex) {
    LOG.warn("Circuit breaker fallback for getAlerts({}): {}", incidentId, ex.toString());
    return empty();
  }

  private Throwable handleException(Throwable ex) {

    if (!(ex instanceof WebClientResponseException)) {
      LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
      return ex;
    }

    WebClientResponseException wcre = (WebClientResponseException)ex;

    switch (HttpStatus.resolve(wcre.getStatusCode().value())) {

      case NOT_FOUND:
        return new NotFoundException(getErrorMessage(wcre));

      case UNPROCESSABLE_ENTITY:
        return new InvalidInputException(getErrorMessage(wcre));

      default:
        LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
        LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
        return ex;
    }
  }

  private String getErrorMessage(WebClientResponseException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }
}