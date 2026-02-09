package com.job.challenge.infrastructure.out.collections.services;

import com.job.challenge.application.domain.EventType;
import com.job.challenge.application.domain.GetEventsRequest;
import com.job.challenge.application.domain.S3Event;
import com.job.challenge.infrastructure.out.collections.documents.EventTypeDocument;
import com.job.challenge.infrastructure.out.collections.documents.S3EventDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
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
    private ReactiveMongoTemplate reactiveMongoTemplate;

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
        when(reactiveMongoTemplate.find(any(Query.class), eq(S3EventDocument.class)))
                .thenReturn(Flux.just(testDocument));

        StepVerifier.create(s3EventCollection.get("test-bucket", pageRequest))
                .expectNextMatches(event -> 
                    event.getBucketName().equals("test-bucket") &&
                    event.getObjectKey().equals("test-key") &&
                    event.getType() == EventType.OBJECT_CREATED
                )
                .verifyComplete();

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(reactiveMongoTemplate).find(queryCaptor.capture(), eq(S3EventDocument.class));
        
        Query capturedQuery = queryCaptor.getValue();
        assertNotNull(capturedQuery);
    }

    @Test
    @DisplayName("Should return empty flux when no events found")
    void shouldReturnEmptyFluxWhenNoEventsFound() {
        when(reactiveMongoTemplate.find(any(Query.class), eq(S3EventDocument.class)))
                .thenReturn(Flux.empty());

        StepVerifier.create(s3EventCollection.get("test-bucket", pageRequest))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return true when event exists")
    void shouldReturnTrueWhenEventExists() {
        when(reactiveMongoTemplate.exists(any(Query.class), eq(S3EventDocument.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(s3EventCollection.exist(testEvent))
                .expectNext(true)
                .verifyComplete();

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(reactiveMongoTemplate).exists(queryCaptor.capture(), eq(S3EventDocument.class));
        
        Query capturedQuery = queryCaptor.getValue();
        assertNotNull(capturedQuery);
    }

    @Test
    @DisplayName("Should return false when event does not exist")
    void shouldReturnFalseWhenEventDoesNotExist() {
        when(reactiveMongoTemplate.exists(any(Query.class), eq(S3EventDocument.class)))
                .thenReturn(Mono.just(false));

        StepVerifier.create(s3EventCollection.exist(testEvent))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save event and return id")
    void shouldSaveEventAndReturnId() {
        ObjectId savedId = new ObjectId("507f1f77bcf86cd799439012");
        S3EventDocument savedDocument = S3EventDocument.builder()
                .id(savedId)
                .bucketName(testDocument.getBucketName())
                .objectKey(testDocument.getObjectKey())
                .type(testDocument.getType())
                .time(testDocument.getTime())
                .objectSize(testDocument.getObjectSize())
                .build();

        when(reactiveMongoTemplate.save(any(S3EventDocument.class)))
                .thenReturn(Mono.just(savedDocument));

        StepVerifier.create(s3EventCollection.save(testEvent))
                .expectNext("507f1f77bcf86cd799439012")
                .verifyComplete();

        ArgumentCaptor<S3EventDocument> documentCaptor = ArgumentCaptor.forClass(S3EventDocument.class);
        verify(reactiveMongoTemplate).save(documentCaptor.capture());
        
        S3EventDocument capturedDocument = documentCaptor.getValue();
        assertEquals("test-bucket", capturedDocument.getBucketName());
        assertEquals("test-key", capturedDocument.getObjectKey());
        assertEquals(EventTypeDocument.OBJECT_CREATED, capturedDocument.getType());
    }

    @Test
    @DisplayName("Should save event without id and return generated id")
    void shouldSaveEventWithoutIdAndReturnGeneratedId() {
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

        when(reactiveMongoTemplate.save(any(S3EventDocument.class)))
                .thenReturn(Mono.just(savedDocument));

        StepVerifier.create(s3EventCollection.save(eventWithoutId))
                .expectNext("507f1f77bcf86cd799439013")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should propagate error when exists check fails")
    void shouldPropagateErrorWhenExistsCheckFails() {
        when(reactiveMongoTemplate.exists(any(Query.class), eq(S3EventDocument.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(s3EventCollection.exist(testEvent))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should propagate error when save fails")
    void shouldPropagateErrorWhenSaveFails() {
        when(reactiveMongoTemplate.save(any(S3EventDocument.class)))
                .thenReturn(Mono.error(new RuntimeException("Save error")));

        StepVerifier.create(s3EventCollection.save(testEvent))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should propagate error when find fails")
    void shouldPropagateErrorWhenFindFails() {
        when(reactiveMongoTemplate.find(any(Query.class), eq(S3EventDocument.class)))
                .thenReturn(Flux.error(new RuntimeException("Find error")));

        StepVerifier.create(s3EventCollection.get("test-bucket", pageRequest))
                .expectError(RuntimeException.class)
                .verify();
    }
}
