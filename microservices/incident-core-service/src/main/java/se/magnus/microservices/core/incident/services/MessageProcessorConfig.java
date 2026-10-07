package se.magnus.microservices.core.incident.services;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.magnus.api.core.incident.Incident;
import se.magnus.api.core.incident.IncidentService;
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.EventProcessingException;
import se.magnus.util.messaging.IdempotencyHandler;

@Configuration
public class MessageProcessorConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final IncidentService incidentService;
  private final IdempotencyHandler idempotencyHandler = new IdempotencyHandler();

  @Autowired
  public MessageProcessorConfig(IncidentService incidentService) {
    this.incidentService = incidentService;
  }

  @Bean
  public Consumer<Event<Integer, Incident>> messageProcessor() {
    return event -> {
      LOG.info("Process message created at {}...", event.getEventCreatedAt());

      if (!idempotencyHandler.beginProcessing(event)) {
        LOG.warn("Skipping duplicate incident event created at {}", event.getEventCreatedAt());
        return;
      }

      switch (event.getEventType()) {

        case CREATE:
          Incident incident = event.getData();
          LOG.info("Create incident with ID: {}", incident.getIncidentId());
          incidentService.createIncident(incident)
              .doOnSuccess(createdIncident -> {
                LOG.info("Incident created: {}", createdIncident.getIncidentId());
                idempotencyHandler.markProcessed(event);
              })
              .doOnError(error -> {
                idempotencyHandler.markFailed(event);
                LOG.error("Failed to create incident", error);
              })
              .subscribe(
                  created -> LOG.debug("Success"),
                  error -> LOG.error("Error", error),
                  () -> LOG.debug("Completed")
              );
          break;

        case DELETE:
          int incidentId = event.getKey();
          LOG.info("Delete incident with IncidentID: {}", incidentId);
          incidentService.deleteIncident(incidentId)
              .doOnSuccess(result -> {
                LOG.info("Incident deleted: {}", incidentId);
                idempotencyHandler.markProcessed(event);
              })
              .doOnError(error -> {
                idempotencyHandler.markFailed(event);
                LOG.error("Failed to delete incident", error);
              })
              .subscribe(
                  result -> LOG.debug("Success"),
                  error -> LOG.error("Error", error),
                  () -> LOG.debug("Completed")
              );
          break;

        default:
          String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
          LOG.warn(errorMessage);
          idempotencyHandler.markFailed(event);
          throw new EventProcessingException(errorMessage);
      }

      LOG.info("Message processing done!");

    };
  }
}
