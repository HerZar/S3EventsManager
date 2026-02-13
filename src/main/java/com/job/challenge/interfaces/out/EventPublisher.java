package com.job.challenge.interfaces.out;

import com.job.challenge.application.domain.Event;
import reactor.core.publisher.Mono;

public interface EventPublisher {
    Mono<Void> publish(Event event);
}
