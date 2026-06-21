package se.magnus.api.composite.incident;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "ProductComposite", description = "REST API for composite incident information.")
public interface IncidentCompositeService {

  /**
   * Sample usage, see below.
   *
   * curl -X POST $HOST:$PORT/incident-composite \
   *   -H "Content-Type: application/json" --data \
   *   '{"productId":123,"name":"incident 123","weight":123}'
   *
   * @param body A JSON representation of the new composite incident
   */
  @Operation(
    summary = "${api.incident-composite.create-composite-incident.description}",
    description = "${api.incident-composite.create-composite-incident.notes}")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
    @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
  })
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PostMapping(
    value    = "/incident-composite",
    consumes = "application/json")
  Mono<Void> createIncident(@RequestBody IncidentAggregate body);

  /**
   * Sample usage: "curl $HOST:$PORT/incident-composite/1".
   *
   * @param productId Id of the incident
   * @return the composite incident info, if found, else null
   */
  @Operation(
    summary = "${api.incident-composite.get-composite-incident.description}",
    description = "${api.incident-composite.get-composite-incident.notes}")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
    @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
    @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
    @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
  })
  @GetMapping(
    value = "/incident-composite/{incidentId}",
    produces = "application/json")
  Mono<IncidentAggregate> getIncident(@PathVariable int incidentId);

  /**
   * Sample usage: "curl -X DELETE $HOST:$PORT/incident-composite/1".
   *
   * @param productId Id of the incident
   */
  @Operation(
    summary = "${api.incident-composite.delete-composite-incident.description}",
    description = "${api.incident-composite.delete-composite-incident.notes}")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
    @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
  })
  @ResponseStatus(HttpStatus.ACCEPTED)
  @DeleteMapping(value = "/incident-composite/{incidentId}")
  Mono<Void> deleteIncident(@PathVariable int incidentId);
}
