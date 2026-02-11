package com.job.challenge.infrastructure.out.collections.services.strategy;

import com.job.challenge.infrastructure.out.collections.documents.S3EventDocument;
import com.job.challenge.infrastructure.out.collections.repositories.S3EventReactiveRepository;
import reactor.core.publisher.Mono;

public class DefaultExistenceStrategy implements ExistenceStrategy {
    
    @Override
    public Mono<Boolean> checkExistence(S3EventDocument document, S3EventReactiveRepository repository) {
        return repository.existsByBucketNameAndObjectKeyAndType(
            document.getBucketName(), 
            document.getObjectKey(), 
            document.getType()
        );
    }
}
