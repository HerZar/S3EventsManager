package com.job.challenge.infrastructure.out.collections.repositories;

import com.job.challenge.infrastructure.out.collections.documents.EventTypeDocument;
import com.job.challenge.infrastructure.out.collections.documents.S3EventDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface S3EventReactiveRepository extends ReactiveMongoRepository<S3EventDocument, String> {
    
    Flux<S3EventDocument> findByBucketNameOrderByTimeDesc(String bucketName, Pageable pageable);
    
    Mono<Boolean> existsByBucketNameAndObjectKeyAndType(
        String bucketName, String objectKey, EventTypeDocument type);
    
    Mono<Boolean> existsByBucketNameAndObjectKeyAndTypeAndTime(
        String bucketName, String objectKey, EventTypeDocument type, LocalDateTime time);
}
