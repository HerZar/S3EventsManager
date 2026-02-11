package com.job.challenge.application.service;

import com.job.challenge.application.domain.S3Event;
import com.job.challenge.application.domain.GetEventsRequest;
import com.job.challenge.interfaces.in.S3EventService;
import com.job.challenge.interfaces.out.S3EventCollection;
import com.job.challenge.interfaces.out.S3EventPublisher;
import com.job.challenge.application.exceptions.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class S3EventServiceImpl implements S3EventService {

    private final S3EventCollection s3EventCollection;
    private final S3EventPublisher s3EventPublisher;

    @Autowired
    public S3EventServiceImpl(S3EventCollection s3EventCollection, S3EventPublisher s3EventPublisher) {
        this.s3EventCollection = s3EventCollection;
        this.s3EventPublisher = s3EventPublisher;
    }

    @Override
    public Flux<S3Event> get(String bucketName, GetEventsRequest pageRequest) {
        return s3EventCollection.get(bucketName, pageRequest);
    }

    @Override
    public Mono<String> create(S3Event event) {
        return s3EventCollection.exist(event)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("S3Event already exists"));
                    }
                    return s3EventCollection.save(event)
                            .onErrorMap(e -> new ConflictException("Failed operation save: " + event, "DATABASE_SAVE_ERROR"))
                            .flatMap(savedEventId -> s3EventPublisher.publish(event)
                                    .thenReturn(savedEventId));
                });
    }
}
