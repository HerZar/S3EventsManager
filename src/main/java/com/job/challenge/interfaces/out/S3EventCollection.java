package com.job.challenge.interfaces.out;

import com.job.challenge.application.domain.S3Event;
import com.job.challenge.application.domain.GetEventsRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface S3EventCollection {

    Flux<S3Event> get(String bucketName, GetEventsRequest pageRequest);

    Mono<Boolean> exist(S3Event event);

    Mono<S3Event> save(S3Event event);
}
