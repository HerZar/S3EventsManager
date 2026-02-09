package com.job.challenge.interfaces.out;

import com.job.challenge.application.domain.S3Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface S3EventCollection {

    Flux<S3Event> get(String bucketName);

    Mono<Boolean> exist(S3Event event);

    Mono<String> save(S3Event event);
}
