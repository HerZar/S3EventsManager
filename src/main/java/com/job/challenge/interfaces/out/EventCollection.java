package com.job.challenge.interfaces.out;

import com.job.challenge.application.domain.Event;
import com.job.challenge.application.domain.GetEventsRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventCollection {

    Flux<Event> get(String bucketName, GetEventsRequest pageRequest);

    Mono<Boolean> exist(Event event);

    Mono<Event> save(Event event);
}
