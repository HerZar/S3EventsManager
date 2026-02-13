package com.job.challenge.infrastructure.out.queue.services;

import com.job.challenge.application.domain.Event;
import com.job.challenge.infrastructure.out.queue.dto.Message;
import com.job.challenge.infrastructure.out.queue.mappers.MessageMapper;
import com.job.challenge.interfaces.out.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EventPublisherImpl implements EventPublisher {

    private final AWSSqsService awsSqsService;

    @Autowired
    public EventPublisherImpl(AWSSqsService awsSqsService) {
        this.awsSqsService = awsSqsService;
    }

    @Override
    public Mono<Void> publish(Event event) {
        Message message = MessageMapper.toMessage(event);
        return awsSqsService.publish(message);
    }
}
