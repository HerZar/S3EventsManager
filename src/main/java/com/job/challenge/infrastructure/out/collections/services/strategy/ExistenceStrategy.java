package com.job.challenge.infrastructure.out.collections.services.strategy;

import com.job.challenge.infrastructure.out.collections.documents.S3EventDocument;
import com.job.challenge.infrastructure.out.collections.repositories.S3EventReactiveRepository;
import reactor.core.publisher.Mono;

public interface ExistenceStrategy {
    Mono<Boolean> checkExistence(S3EventDocument document, S3EventReactiveRepository repository);
}
