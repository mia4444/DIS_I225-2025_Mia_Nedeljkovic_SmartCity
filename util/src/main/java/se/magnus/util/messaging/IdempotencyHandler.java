package se.magnus.util.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import se.magnus.api.event.Event;

public class IdempotencyHandler {

  private static final Duration DEFAULT_RETENTION = Duration.ofHours(24);

  private final Duration retentionPeriod;
  private final ObjectMapper objectMapper;
  private final ConcurrentHashMap<String, ProcessingState> eventStates;

  public IdempotencyHandler() {
    this(DEFAULT_RETENTION);
  }

  public IdempotencyHandler(Duration retentionPeriod) {
    this(retentionPeriod, new ObjectMapper());
  }

  public IdempotencyHandler(Duration retentionPeriod, ObjectMapper objectMapper) {
    this.retentionPeriod = Objects.requireNonNull(retentionPeriod, "retentionPeriod must not be null");
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    this.eventStates = new ConcurrentHashMap<>();
  }

  public boolean beginProcessing(Event<?, ?> event) {
    purgeExpiredEntries();

    String fingerprint = fingerprint(event);
    ProcessingState newState = new ProcessingState(Status.IN_PROGRESS, Instant.now());
    return eventStates.putIfAbsent(fingerprint, newState) == null;
  }

  public void markProcessed(Event<?, ?> event) {
    String fingerprint = fingerprint(event);
    eventStates.computeIfPresent(fingerprint, (key, existingState) -> new ProcessingState(Status.PROCESSED, Instant.now()));
  }

  public void markFailed(Event<?, ?> event) {
    eventStates.remove(fingerprint(event));
  }

  private void purgeExpiredEntries() {
    Instant cutoff = Instant.now().minus(retentionPeriod);
    eventStates.entrySet().removeIf(entry -> entry.getValue().updatedAt.isBefore(cutoff));
  }

  private String fingerprint(Event<?, ?> event) {
    Objects.requireNonNull(event, "event must not be null");

    try {
      String payload = objectMapper.writeValueAsString(event);
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (JsonProcessingException | NoSuchAlgorithmException e) {
      throw new IllegalStateException("Failed to create idempotency fingerprint", e);
    }
  }

  private enum Status {
    IN_PROGRESS,
    PROCESSED
  }

  private record ProcessingState(Status status, Instant updatedAt) {}
}

