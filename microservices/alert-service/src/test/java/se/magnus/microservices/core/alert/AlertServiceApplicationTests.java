package se.magnus.microservices.core.alert;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.alert.Alert;
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.InvalidInputException;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
  "spring.cloud.stream.defaultBinder=rabbit",
  "logging.level.se.magnus=DEBUG",
  "eureka.client.enabled=false"})
@Tag("docker")
class AlertServiceApplicationTests extends MySqlTestBase {

  @Autowired
  private WebTestClient client;

  @Autowired
  private se.magnus.microservices.core.alert.persistence.AlertRepository repository;

  @Autowired
  @Qualifier("messageProcessor")
  private Consumer<Event<Integer, Alert>> messageProcessor;

  @BeforeEach
  void setupDb() {
    repository.deleteAll();
  }

  @Test
  void getAlertsByIncidentId() {

    int incidentId = 1;

    assertEquals(0, repository.findByIncidentId(incidentId).size());

    sendCreateAlertEvent(incidentId, 1);
    sendCreateAlertEvent(incidentId, 2);
    sendCreateAlertEvent(incidentId, 3);

    assertEquals(3, repository.findByIncidentId(incidentId).size());

    getAndVerifyAlertsByIncidentId(incidentId, OK)
      .jsonPath("$.length()").isEqualTo(3)
      .jsonPath("$[2].incidentId").isEqualTo(incidentId)
      .jsonPath("$[2].alertId").isEqualTo(3);
  }

  @Test
  void duplicateError() {

    int incidentId = 1;
    int alertId = 1;

    assertEquals(0, repository.count());

    sendCreateAlertEvent(incidentId, alertId);

    assertEquals(1, repository.count());

    InvalidInputException thrown = assertThrows(
      InvalidInputException.class,
      () -> sendCreateAlertEvent(incidentId, alertId),
      "Expected a InvalidInputException here!");
    assertEquals("Duplicate key, Incident Id: 1, Alert Id:1", thrown.getMessage());

    assertEquals(1, repository.count());
  }

  @Test
  void deleteAlerts() {

    int incidentId = 1;
    int alertId = 1;

    sendCreateAlertEvent(incidentId, alertId);
    assertEquals(1, repository.findByIncidentId(incidentId).size());

    sendDeleteAlertEvent(incidentId);
    assertEquals(0, repository.findByIncidentId(incidentId).size());

    sendDeleteAlertEvent(incidentId);
  }

  @Test
  void getAlertsMissingParameter() {

    getAndVerifyAlertsByIncidentId("", BAD_REQUEST)
      .jsonPath("$.path").isEqualTo("/alert")
      .jsonPath("$.message").isEqualTo("Required query parameter 'incidentId' is not present.");
  }

  @Test
  void getAlertsInvalidParameter() {

    getAndVerifyAlertsByIncidentId("?incidentId=no-integer", BAD_REQUEST)
      .jsonPath("$.path").isEqualTo("/alert")
      .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getAlertsNotFound() {

    getAndVerifyAlertsByIncidentId("?incidentId=213", OK)
      .jsonPath("$.length()").isEqualTo(0);
  }

  @Test
  void getAlertsInvalidParameterNegativeValue() {

    int incidentIdInvalid = -1;

    getAndVerifyAlertsByIncidentId("?incidentId=" + incidentIdInvalid, UNPROCESSABLE_ENTITY)
      .jsonPath("$.path").isEqualTo("/alert")
      .jsonPath("$.message").isEqualTo("Invalid incidentId: " + incidentIdInvalid);
  }

  private WebTestClient.BodyContentSpec getAndVerifyAlertsByIncidentId(int incidentId, HttpStatus expectedStatus) {
    return getAndVerifyAlertsByIncidentId("?incidentId=" + incidentId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyAlertsByIncidentId(String incidentIdQuery, HttpStatus expectedStatus) {
    return client.get()
      .uri("/alert" + incidentIdQuery)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus)
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody();
  }

  private void sendCreateAlertEvent(int incidentId, int alertId) {
    Alert alert = new Alert(incidentId, alertId, "Author " + alertId, "Subject " + alertId, "Content " + alertId, "SA");
    Event<Integer, Alert> event = new Event(CREATE, incidentId, alert);
    messageProcessor.accept(event);
  }

  private void sendDeleteAlertEvent(int incidentId) {
    Event<Integer, Alert> event = new Event(DELETE, incidentId, null);
    messageProcessor.accept(event);
  }
}
