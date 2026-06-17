package se.magnus.microservices.core.product.services;

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

@Configuration
public class MessageProcessorConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final IncidentService incidentService;

  @Autowired
  public MessageProcessorConfig(IncidentService incidentService) {
    this.incidentService = incidentService;
  }

  @Bean
  public Consumer<Event<Integer, Incident>> messageProcessor() {
    return event -> {
      LOG.info("Process message created at {}...", event.getEventCreatedAt());

      switch (event.getEventType()) {

        case CREATE:
          Incident incident = event.getData();
          LOG.info("Create incident with ID: {}", incident.getProductId());
          incidentService.createProduct(incident).block();
          break;

        case DELETE:
          int productId = event.getKey();
          LOG.info("Delete incident with ProductID: {}", productId);
          incidentService.deleteProduct(productId).block();
          break;

        default:
          String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
          LOG.warn(errorMessage);
          throw new EventProcessingException(errorMessage);
      }

      LOG.info("Message processing done!");

    };
  }
}
