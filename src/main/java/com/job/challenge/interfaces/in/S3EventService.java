package com.job.challenge.interfaces.in;

import com.job.challenge.application.domain.S3Event;
import com.job.challenge.application.domain.GetEventsRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface S3EventService {

    Flux<S3Event> get(String bucketName, GetEventsRequest pageRequest);

    Mono<String> create(S3Event event);
}
