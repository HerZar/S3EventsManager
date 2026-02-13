package com.job.challenge.application.service;

import com.job.challenge.application.domain.Event;
import com.job.challenge.application.domain.GetEventsRequest;
import com.job.challenge.interfaces.in.EventService;
import com.job.challenge.interfaces.out.EventCollection;
import com.job.challenge.interfaces.out.EventPublisher;
import com.job.challenge.application.exceptions.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EventServiceImpl implements EventService {

    private final EventCollection eventCollection;
    private final EventPublisher eventPublisher;

    @Autowired
    public EventServiceImpl(EventCollection eventCollection, EventPublisher eventPublisher) {
        this.eventCollection = eventCollection;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Flux<Event> get(String bucketName, GetEventsRequest pageRequest) {
        return eventCollection.get(bucketName, pageRequest);
    }

    @Override
    public Mono<String> create(Event event) {
        return eventCollection.exist(event)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("Event already exists"));
                    }
                    return eventCollection.save(event)
                            .onErrorMap(e -> new ConflictException("Failed operation save: " + event, "DATABASE_SAVE_ERROR"))
                            .flatMap(savedEvent -> eventPublisher.publish(savedEvent)
                                    .thenReturn(savedEvent.getId()));
                });
    }
}
