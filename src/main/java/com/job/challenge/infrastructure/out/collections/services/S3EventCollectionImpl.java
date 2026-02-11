package com.job.challenge.infrastructure.out.collections.services;

import com.job.challenge.application.domain.S3Event;
import com.job.challenge.infrastructure.out.collections.documents.S3EventDocument;
import com.job.challenge.infrastructure.out.collections.mappers.S3EventDocumentMapper;
import com.job.challenge.infrastructure.out.collections.repositories.S3EventReactiveRepository;
import com.job.challenge.infrastructure.out.collections.services.strategy.ExistenceStrategyFactory;
import com.job.challenge.interfaces.out.S3EventCollection;
import com.job.challenge.application.domain.GetEventsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class S3EventCollectionImpl implements S3EventCollection {

    private final S3EventReactiveRepository s3EventReactiveRepository;
    private final ExistenceStrategyFactory existenceStrategyFactory;

    @Autowired
    public S3EventCollectionImpl(S3EventReactiveRepository s3EventReactiveRepository, 
                                ExistenceStrategyFactory existenceStrategyFactory) {
        this.s3EventReactiveRepository = s3EventReactiveRepository;
        this.existenceStrategyFactory = existenceStrategyFactory;
    }

    @Override
    public Flux<S3Event> get(String bucketName, GetEventsRequest pageRequest) {
        Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize());
        return s3EventReactiveRepository.findByBucketNameOrderByTimeDesc(bucketName, pageable)
                .map(S3EventDocumentMapper::toS3Event);
    }

    @Override
    public Mono<Boolean> exist(S3Event event) {
        S3EventDocument document = S3EventDocumentMapper.toS3EventDocument(event);
        return existenceStrategyFactory.getStrategy(document.getType())
                .checkExistence(document, s3EventReactiveRepository);
    }

    @Override
    public Mono<String> save(S3Event event) {
        S3EventDocument document = S3EventDocumentMapper.toS3EventDocument(event);
        return s3EventReactiveRepository.save(document)
                .map(savedDocument -> savedDocument.getId().toString());
    }
}
