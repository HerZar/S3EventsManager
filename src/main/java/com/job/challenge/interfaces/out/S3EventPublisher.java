package com.job.challenge.interfaces.out;

import com.job.challenge.application.domain.S3Event;
import reactor.core.publisher.Mono;

public interface S3EventPublisher {
    Mono<Void> publish(S3Event event);
}
