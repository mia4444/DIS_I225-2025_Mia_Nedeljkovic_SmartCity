package se.magnus.util.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import se.magnus.api.core.incident.Incident;
import se.magnus.api.event.Event;

class IdempotencyHandlerTests {

  @Test
  void shouldAllowFirstProcessingAndRejectDuplicate() {
    IdempotencyHandler handler = new IdempotencyHandler(Duration.ofMinutes(10));
    Event<Integer, Incident> event = new Event<>(Event.Type.CREATE, 1, new Incident(1, "incident", 1, null));

    assertTrue(handler.beginProcessing(event));
    assertFalse(handler.beginProcessing(event));
  }

  @Test
  void shouldAllowRetryAfterFailure() {
    IdempotencyHandler handler = new IdempotencyHandler(Duration.ofMinutes(10));
    Event<Integer, Incident> event = new Event<>(Event.Type.CREATE, 2, new Incident(2, "incident", 1, null));

    assertTrue(handler.beginProcessing(event));
    handler.markFailed(event);
    assertTrue(handler.beginProcessing(event));
  }

  @Test
  void shouldKeepProcessedEventAsDuplicate() {
    IdempotencyHandler handler = new IdempotencyHandler(Duration.ofMinutes(10));
    Event<Integer, Incident> event = new Event<>(Event.Type.DELETE, 3, null);

    assertTrue(handler.beginProcessing(event));
    handler.markProcessed(event);
    assertFalse(handler.beginProcessing(event));
  }
}

