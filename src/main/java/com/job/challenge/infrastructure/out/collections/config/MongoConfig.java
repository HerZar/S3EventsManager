package com.job.challenge.infrastructure.out.collections.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Configuration
public class MongoConfig {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public MongoConfig(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @PostConstruct
    public void initDatabase() {
        createCollectionIfNotExists()
            .then(createIndexIfNotExists())
            .subscribe();
    }

    private Mono<Void> createCollectionIfNotExists() {
        return reactiveMongoTemplate.collectionExists("s3_events")
            .flatMap(exists -> {
                if (!exists) {
                    return reactiveMongoTemplate.createCollection("s3_events").then();
                }
                return Mono.empty();
            });
    }

    private Mono<Void> createIndexIfNotExists() {
        ReactiveIndexOperations indexOps = reactiveMongoTemplate.indexOps("s3_events");
        
        return indexOps.getIndexInfo()
            .any(indexInfo -> "bucketName_1_objectKey_1_type_1_time_-1".equals(indexInfo.getName()))
            .flatMap(indexExists -> {
                if (!indexExists) {
                    Index index = new Index()
                        .on("bucketName", Sort.Direction.ASC)
                        .on("objectKey", Sort.Direction.ASC)
                        .on("type", Sort.Direction.ASC)
                        .on("time", Sort.Direction.DESC)
                        .named("bucketName_1_objectKey_1_type_1_time_1")
                        .unique();
                    
                    return indexOps.ensureIndex(index).then();
                }
                return Mono.empty();
            });
    }
}
