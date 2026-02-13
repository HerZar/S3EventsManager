package com.job.challenge.interfaces.in;

import com.job.challenge.application.domain.Event;
import com.job.challenge.application.domain.GetEventsRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventService {

    Flux<Event> get(String bucketName, GetEventsRequest pageRequest);

    Mono<String> create(Event event);
}
