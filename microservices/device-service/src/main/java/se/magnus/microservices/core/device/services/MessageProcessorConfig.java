package se.magnus.microservices.core.device.services;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.magnus.api.core.device.Device;
import se.magnus.api.core.device.DeviceService;
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.EventProcessingException;
import se.magnus.util.messaging.IdempotencyHandler;

@Configuration
public class MessageProcessorConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final DeviceService deviceService;
  private final IdempotencyHandler idempotencyHandler = new IdempotencyHandler();

  @Autowired
  public MessageProcessorConfig(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @Bean
  public Consumer<Event<Integer, Device>> messageProcessor() {
    return event -> {
      LOG.info("Process message created at {}...", event.getEventCreatedAt());

      if (!idempotencyHandler.beginProcessing(event)) {
        LOG.warn("Skipping duplicate device event created at {}", event.getEventCreatedAt());
        return;
      }

      switch (event.getEventType()) {

        case CREATE:
          Device device = event.getData();
          LOG.info("Create device with ID: {}", device.getIncidentId());
          deviceService.createDevice(device)
              .doOnSuccess(createdDevice -> {
                LOG.info("Device created for incident: {}", createdDevice.getIncidentId());
                idempotencyHandler.markProcessed(event);
              })
              .doOnError(error -> {
                idempotencyHandler.markFailed(event);
                LOG.error("Failed to create device", error);
              })
              .subscribe(
                  created -> LOG.debug("Success"),
                  error -> LOG.error("Error", error),
                  () -> LOG.debug("Completed")
              );
          break;

        case DELETE:
          int incidentId = event.getKey();
          LOG.info("Delete devices with IncidentID: {}", incidentId);
          deviceService.deleteDevices(incidentId)
              .doOnSuccess(result -> {
                LOG.info("Devices deleted for incident: {}", incidentId);
                idempotencyHandler.markProcessed(event);
              })
              .doOnError(error -> {
                idempotencyHandler.markFailed(event);
                LOG.error("Failed to delete devices", error);
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
