package se.magnus.microservices.core.telemetry.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.telemetry.Telemetry;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.microservices.core.telemetry.persistence.TelemetryEntity;
import se.magnus.microservices.core.telemetry.persistence.TelemetryRepository;
import se.magnus.util.http.ServiceUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TelemetryServiceImplTest {

    private TelemetryRepository repository;
    private TelemetryMapper mapper;
    private ServiceUtil serviceUtil;
    private TelemetryServiceImpl service;

    @BeforeEach
    void setUp(){
        repository=mock(TelemetryRepository.class);
        mapper=mock(TelemetryMapper.class);
        serviceUtil=mock(ServiceUtil.class);
        service=new TelemetryServiceImpl(repository, mapper, serviceUtil);
    }

    @Test
    void CreateReading_withInvalidDeviceId_throwsException(){
       Telemetry body=new Telemetry(0,1,"temperature", 24, "C", null);
        assertThrows(InvalidInputException.class, () -> service.createReading(body));
    }

    @Test
     void createReading_withValidData_savesEntity(){

        Telemetry body=new Telemetry(1,1,"temperature", 24, "C", null);
        TelemetryEntity entity=new TelemetryEntity(null, null,1,1,"temperature", 24, "C");
       when (mapper.apiToEntity(body)).thenReturn(entity);
       when(repository.save(entity)).thenReturn(Mono.just(entity));
       when (mapper.entityToApi(entity)).thenReturn(body);

       Mono <Telemetry> result=service.createReading(body);

       assertEquals(body, result.block());
       verify (repository, times(1)).save(entity);
    }

    @Test
    void getReadings_withInvalidDeviceId_throwsException(){
        assertThrows(InvalidInputException.class, () -> service.getReadings(0));
    }

    @Test
    void getReadings_withValidData_returnsReadings() {
        TelemetryEntity entity = new TelemetryEntity(null,null,1, 1, "temperature", 24, "C"); //nije bitno za test, jer mongoDB property-jima id i version sam dodeljuje null
        Telemetry telemetry = new Telemetry(1, 1, "temperature", 24, "C", null);

        when(repository.findByDeviceId(1)).thenReturn(Flux.just(entity));
        when(mapper.entityToApi(entity)).thenReturn(telemetry);
        when(serviceUtil.getServiceAddress()).thenReturn("localhost:8080");

        List<Telemetry> result = service.getReadings(1).collectList().block();

        assertEquals(1, result.size());
        assertEquals("temperature", result.get(0).getSensorType());
    }
    @Test
    void deleteReadings_withValidDeviceId_callsRepository(){
        when(repository.findByDeviceId(1)).thenReturn(Flux.empty());
        when(repository.deleteAll(any(Flux.class))).thenReturn(Mono.empty());

        service.deleteReadings(1).block();

        verify(repository, times(1)).deleteAll(any(Flux.class));
    }
}