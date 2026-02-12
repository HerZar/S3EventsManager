package com.job.challenge.application.service;

import com.job.challenge.application.domain.EventType;
import com.job.challenge.application.domain.GetEventsRequest;
import com.job.challenge.application.domain.S3Event;
import com.job.challenge.application.exceptions.ConflictException;
import com.job.challenge.interfaces.out.S3EventCollection;
import com.job.challenge.interfaces.out.S3EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3EventServiceImplTest {

    @Mock
    private S3EventCollection s3EventCollection;

    @Mock
    private S3EventPublisher s3EventPublisher;

    @InjectMocks
    private S3EventServiceImpl s3EventService;

    private S3Event testEvent;
    private GetEventsRequest pageRequest;

    @BeforeEach
    void setUp() {
        testEvent = S3Event.builder()
                .id("1")
                .bucketName("test-bucket")
                .objectKey("test-key")
                .type(EventType.OBJECT_CREATED)
                .time(LocalDateTime.now())
                .objectSize(1024)
                .build();

        pageRequest = GetEventsRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should return events when get is called")
    void shouldReturnEventsWhenGetIsCalled() {
        when(s3EventCollection.get(eq("test-bucket"), any(GetEventsRequest.class)))
                .thenReturn(Flux.just(testEvent));

        StepVerifier.create(s3EventService.get("test-bucket", pageRequest))
                .expectNext(testEvent)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty flux when no events found")
    void shouldReturnEmptyFluxWhenNoEventsFound() {
        when(s3EventCollection.get(eq("test-bucket"), any(GetEventsRequest.class)))
                .thenReturn(Flux.empty());

        StepVerifier.create(s3EventService.get("test-bucket", pageRequest))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should create event when it does not exist")
    void shouldCreateEventWhenItDoesNotExist() {
        S3Event savedEvent = S3Event.builder()
                .id("1")
                .bucketName("test-bucket")
                .objectKey("test-key")
                .type(EventType.OBJECT_CREATED)
                .time(testEvent.getTime())
                .objectSize(1024)
                .build();
        
        when(s3EventCollection.exist(testEvent)).thenReturn(Mono.just(false));
        when(s3EventCollection.save(testEvent)).thenReturn(Mono.just(savedEvent));
        when(s3EventPublisher.publish(savedEvent)).thenReturn(Mono.empty());

        StepVerifier.create(s3EventService.create(testEvent))
                .expectNext("1")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw ConflictException when event already exists")
    void shouldThrowConflictExceptionWhenEventAlreadyExists() {
        when(s3EventCollection.exist(testEvent)).thenReturn(Mono.just(true));

        StepVerifier.create(s3EventService.create(testEvent))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    @DisplayName("Should propagate error when exist check fails")
    void shouldPropagateErrorWhenExistCheckFails() {
        when(s3EventCollection.exist(testEvent)).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(s3EventService.create(testEvent))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should propagate error when save fails")
    void shouldPropagateErrorWhenSaveFails() {
        when(s3EventCollection.exist(testEvent)).thenReturn(Mono.just(false));
        when(s3EventCollection.save(testEvent)).thenReturn(Mono.error(new RuntimeException("Save error")));

        StepVerifier.create(s3EventService.create(testEvent))
                .expectErrorMatches(throwable -> 
                    throwable instanceof ConflictException &&
                    throwable.getMessage().contains("Failed operation save:") &&
                    ((ConflictException) throwable).getErrorCode().equals("DATABASE_SAVE_ERROR"))
                .verify();
    }

    @Test
    @DisplayName("Should propagate error when publisher fails")
    void shouldPropagateErrorWhenPublisherFails() {
        S3Event savedEvent = S3Event.builder()
                .id("1")
                .bucketName("test-bucket")
                .objectKey("test-key")
                .type(EventType.OBJECT_CREATED)
                .time(testEvent.getTime())
                .objectSize(1024)
                .build();
        
        when(s3EventCollection.exist(testEvent)).thenReturn(Mono.just(false));
        when(s3EventCollection.save(testEvent)).thenReturn(Mono.just(savedEvent));
        when(s3EventPublisher.publish(savedEvent)).thenReturn(Mono.error(new ConflictException("Publisher error")));

        StepVerifier.create(s3EventService.create(testEvent))
                .expectError(ConflictException.class)
                .verify();
    }
}
