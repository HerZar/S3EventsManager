package com.job.challenge.infrastructure.out.collections.services;

import com.job.challenge.application.domain.EventType;
import com.job.challenge.application.domain.GetEventsRequest;
import com.job.challenge.application.domain.S3Event;
import com.job.challenge.infrastructure.out.collections.documents.EventTypeDocument;
import com.job.challenge.infrastructure.out.collections.documents.S3EventDocument;
import com.job.challenge.infrastructure.out.collections.services.strategy.ExistenceStrategy;
import com.job.challenge.infrastructure.out.collections.services.strategy.ExistenceStrategyFactory;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.job.challenge.infrastructure.out.collections.repositories.S3EventReactiveRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3EventCollectionImplTest {

    @Mock
    private S3EventReactiveRepository s3EventReactiveRepository;

    @Mock
    private ExistenceStrategyFactory existenceStrategyFactory;

    @Mock
    private ExistenceStrategy existenceStrategy;

    @InjectMocks
    private S3EventCollectionImpl s3EventCollection;

    private S3Event testEvent;
    private S3EventDocument testDocument;
    private GetEventsRequest pageRequest;

    @BeforeEach
    void setUp() {
        testEvent = S3Event.builder()
                .id("507f1f77bcf86cd799439011")
                .bucketName("test-bucket")
                .objectKey("test-key")
                .type(EventType.OBJECT_CREATED)
                .time(LocalDateTime.now())
                .objectSize(1024)
                .build();

        testDocument = S3EventDocument.builder()
                .id(new ObjectId("507f1f77bcf86cd799439011"))
                .bucketName("test-bucket")
                .objectKey("test-key")
                .type(EventTypeDocument.OBJECT_CREATED)
                .time(testEvent.getTime())
                .objectSize(1024)
                .build();

        pageRequest = GetEventsRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should return events when get is called")
    void shouldReturnEventsWhenGetIsCalled() {
        when(s3EventReactiveRepository.findByBucketNameOrderByTimeDesc(eq("test-bucket"), any(Pageable.class)))
                .thenReturn(Flux.just(testDocument));

        StepVerifier.create(s3EventCollection.get("test-bucket", pageRequest))
                .expectNextMatches(event -> 
                    event.getBucketName().equals("test-bucket") &&
                    event.getObjectKey().equals("test-key") &&
                    event.getType() == EventType.OBJECT_CREATED
                )
                .verifyComplete();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(s3EventReactiveRepository).findByBucketNameOrderByTimeDesc(eq("test-bucket"), pageableCaptor.capture());
        
        Pageable capturedPageable = pageableCaptor.getValue();
        assertNotNull(capturedPageable);
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());
    }

    @Test
    @DisplayName("Should return empty flux when no events found")
    void shouldReturnEmptyFluxWhenNoEventsFound() {
        when(s3EventReactiveRepository.findByBucketNameOrderByTimeDesc(eq("test-bucket"), any(Pageable.class)))
                .thenReturn(Flux.empty());

        StepVerifier.create(s3EventCollection.get("test-bucket", pageRequest))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return true when event exists")
    void shouldReturnTrueWhenEventExists() {
        when(existenceStrategyFactory.getStrategy(any(EventTypeDocument.class)))
                .thenReturn(existenceStrategy);
        when(existenceStrategy.checkExistence(any(S3EventDocument.class), eq(s3EventReactiveRepository)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(s3EventCollection.exist(testEvent))
                .expectNext(true)
                .verifyComplete();

        verify(existenceStrategy).checkExistence(any(S3EventDocument.class), eq(s3EventReactiveRepository));
    }

    @Test
    @DisplayName("Should return false when event does not exist")
    void shouldReturnFalseWhenEventDoesNotExist() {
        when(existenceStrategyFactory.getStrategy(any(EventTypeDocument.class)))
                .thenReturn(existenceStrategy);
        when(existenceStrategy.checkExistence(any(S3EventDocument.class), eq(s3EventReactiveRepository)))
                .thenReturn(Mono.just(false));

        StepVerifier.create(s3EventCollection.exist(testEvent))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save event and return saved event")
    void shouldSaveEventAndReturnSavedEvent() {
        ObjectId savedId = new ObjectId("507f1f77bcf86cd799439012");
        S3EventDocument savedDocument = S3EventDocument.builder()
                .id(savedId)
                .bucketName(testDocument.getBucketName())
                .objectKey(testDocument.getObjectKey())
                .type(testDocument.getType())
                .time(testDocument.getTime())
                .objectSize(testDocument.getObjectSize())
                .build();

        S3Event expectedSavedEvent = S3Event.builder()
                .id("507f1f77bcf86cd799439012")
                .bucketName("test-bucket")
                .objectKey("test-key")
                .type(EventType.OBJECT_CREATED)
                .time(testEvent.getTime())
                .objectSize(1024)
                .build();

        when(s3EventReactiveRepository.save(any(S3EventDocument.class)))
                .thenReturn(Mono.just(savedDocument));

        StepVerifier.create(s3EventCollection.save(testEvent))
                .expectNextMatches(savedEvent -> 
                    savedEvent.getId().equals("507f1f77bcf86cd799439012") &&
                    savedEvent.getBucketName().equals("test-bucket") &&
                    savedEvent.getObjectKey().equals("test-key") &&
                    savedEvent.getType() == EventType.OBJECT_CREATED
                )
                .verifyComplete();

        ArgumentCaptor<S3EventDocument> documentCaptor = ArgumentCaptor.forClass(S3EventDocument.class);
        verify(s3EventReactiveRepository).save(documentCaptor.capture());
        
        S3EventDocument capturedDocument = documentCaptor.getValue();
        assertEquals("test-bucket", capturedDocument.getBucketName());
        assertEquals("test-key", capturedDocument.getObjectKey());
        assertEquals(EventTypeDocument.OBJECT_CREATED, capturedDocument.getType());
    }

    @Test
    @DisplayName("Should save event without id and return saved event with generated id")
    void shouldSaveEventWithoutIdAndReturnSavedEventWithGeneratedId() {
        S3Event eventWithoutId = S3Event.builder()
                .bucketName("test-bucket")
                .objectKey("test-key")
                .type(EventType.OBJECT_CREATED)
                .time(testEvent.getTime())
                .objectSize(1024)
                .build();
        ObjectId savedId = new ObjectId("507f1f77bcf86cd799439013");
        S3EventDocument savedDocument = S3EventDocument.builder()
                .id(savedId)
                .bucketName("test-bucket")
                .objectKey("test-key")
                .type(EventTypeDocument.OBJECT_CREATED)
                .time(testEvent.getTime())
                .objectSize(1024)
                .build();

        when(s3EventReactiveRepository.save(any(S3EventDocument.class)))
                .thenReturn(Mono.just(savedDocument));

        StepVerifier.create(s3EventCollection.save(eventWithoutId))
                .expectNextMatches(savedEvent -> 
                    savedEvent.getId().equals("507f1f77bcf86cd799439013") &&
                    savedEvent.getBucketName().equals("test-bucket") &&
                    savedEvent.getObjectKey().equals("test-key") &&
                    savedEvent.getType() == EventType.OBJECT_CREATED
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should propagate error when exists check fails")
    void shouldPropagateErrorWhenExistsCheckFails() {
        when(existenceStrategyFactory.getStrategy(any(EventTypeDocument.class)))
                .thenReturn(existenceStrategy);
        when(existenceStrategy.checkExistence(any(S3EventDocument.class), eq(s3EventReactiveRepository)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(s3EventCollection.exist(testEvent))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should propagate error when save fails")
    void shouldPropagateErrorWhenSaveFails() {
        when(s3EventReactiveRepository.save(any(S3EventDocument.class)))
                .thenReturn(Mono.error(new RuntimeException("Save error")));

        StepVerifier.create(s3EventCollection.save(testEvent))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should propagate error when find fails")
    void shouldPropagateErrorWhenFindFails() {
        when(s3EventReactiveRepository.findByBucketNameOrderByTimeDesc(eq("test-bucket"), any(Pageable.class)))
                .thenReturn(Flux.error(new RuntimeException("Find error")));

        StepVerifier.create(s3EventCollection.get("test-bucket", pageRequest))
                .expectError(RuntimeException.class)
                .verify();
    }
}
