package com.job.challenge.infrastructure.out.collections.services;

import com.job.challenge.application.domain.S3Event;
import com.job.challenge.infrastructure.out.collections.documents.S3EventDocument;
import com.job.challenge.infrastructure.out.collections.mappers.S3EventDocumentMapper;
import com.job.challenge.interfaces.out.S3EventCollection;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Flux<S3Event> get(String bucketName) {
        Query query = new Query(Criteria.where("bucketName").is(bucketName));
        return reactiveMongoTemplate.find(query, S3EventDocument.class)
                .map(S3EventDocumentMapper::toS3Event);
    }

    @Override
    public Mono<Boolean> exist(S3Event event) {
        S3EventDocument document = S3EventDocumentMapper.toS3EventDocument(event);
        Query query = new Query(
            Criteria.where("bucketName").is(document.getBucketName())
                .and("objectKey").is(document.getObjectKey())
                .and("type").is(document.getType())
        );
        return reactiveMongoTemplate.exists(query, S3EventDocument.class);
    }

    @Override
    public Mono<String> save(S3Event event) {
        S3EventDocument document = S3EventDocumentMapper.toS3EventDocument(event);
        return reactiveMongoTemplate.save(document)
                .map(savedDocument -> savedDocument.getId().toString());
    }
}
