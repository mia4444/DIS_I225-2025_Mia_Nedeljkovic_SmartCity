package se.magnus.microservices.core.alert.services;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.magnus.api.core.alert.Alert;
import se.magnus.api.core.alert.AlertService;
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.EventProcessingException;
import se.magnus.util.messaging.IdempotencyHandler;

@Configuration
public class MessageProcessorConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final AlertService alertService;
  private final IdempotencyHandler idempotencyHandler = new IdempotencyHandler();

  @Autowired
  public MessageProcessorConfig(AlertService alertService) {
    this.alertService = alertService;
  }

  @Bean
  public Consumer<Event<Integer, Alert>> messageProcessor() {
    return event -> {
      LOG.info("Process message created at {}...", event.getEventCreatedAt());

      if (!idempotencyHandler.beginProcessing(event)) {
        LOG.warn("Skipping duplicate alert event created at {}", event.getEventCreatedAt());
        return;
      }

      switch (event.getEventType()) {

        case CREATE:
          handleCreateEvent(event);
          break;

        case DELETE:
          handleDeleteEvent(event);
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

  private void handleDeleteEvent(Event<Integer, Alert> event) {
    int incidentId = event.getKey();
    LOG.info("Deleting alerts for incidentId: {}", incidentId);

    alertService.deleteAlerts(incidentId)
            .doOnSuccess(result -> {
              LOG.info("Alerts deleted for incident: {}", incidentId);
              idempotencyHandler.markProcessed(event);
            })
            .doOnError(error ->
                    {
                      idempotencyHandler.markFailed(event);
                      LOG.error("Failed to delete alerts", error);
                    })
            .subscribe(
                    result -> LOG.debug("Success"),
                    error -> LOG.error("Error", error),
                    () -> LOG.debug("Completed")
            );
  }

  private void handleCreateEvent(Event<Integer, Alert> event) {
    Alert alert = event.getData();
    LOG.info("Creating alert with ID: {}/{}", alert.getIncidentId(), alert.getAlertId());

    alertService.createAlert(alert)
            .doOnSuccess(createdAlert -> {
              LOG.info("Alert created: {}", createdAlert.getAlertId());
              idempotencyHandler.markProcessed(event);
            })
            .doOnError(error ->
                    {
                      idempotencyHandler.markFailed(event);
                      LOG.error("Failed to create alert", error);
                    })
            .subscribe(
                    created -> LOG.debug("Success"),
                    error -> LOG.error("Error", error),
                    () -> LOG.debug("Completed")
            );
  }
}

