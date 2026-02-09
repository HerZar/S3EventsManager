package com.job.challenge.infrastructure.out.collections.services;

import com.job.challenge.application.domain.S3Event;
import com.job.challenge.infrastructure.out.collections.documents.S3EventDocument;
import com.job.challenge.infrastructure.out.collections.mappers.S3EventDocumentMapper;
import com.job.challenge.interfaces.out.S3EventCollection;
import com.job.challenge.application.domain.GetEventsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class S3EventCollectionImpl implements S3EventCollection {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public S3EventCollectionImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Flux<S3Event> get(String bucketName, GetEventsRequest pageRequest) {
        Query query = new Query(Criteria.where("bucketName").is(bucketName));
        query.with(Sort.by(Sort.Direction.DESC, "time"));
        query.skip((long) pageRequest.getPage() * pageRequest.getSize());
        query.limit(pageRequest.getSize());
        return reactiveMongoTemplate.find(query, S3EventDocument.class)
                .map(S3EventDocumentMapper::toS3Event);
    }

    @Override
    public Mono<Boolean> exist(S3Event event) {
        return Mono.fromCallable(() -> S3EventDocumentMapper.toS3EventDocument(event))
                .flatMap(document -> {
                    Query query = new Query(
                        Criteria.where("bucketName").is(document.getBucketName())
                            .and("objectKey").is(document.getObjectKey())
                            .and("type").is(document.getType())
                    );
                    return reactiveMongoTemplate.exists(query, S3EventDocument.class);
                });
    }

    @Override
    public Mono<String> save(S3Event event) {
        return Mono.fromCallable(() -> S3EventDocumentMapper.toS3EventDocument(event))
                .flatMap(document -> reactiveMongoTemplate.save(document))
                .map(savedDocument -> savedDocument.getId().toString());
    }
}
