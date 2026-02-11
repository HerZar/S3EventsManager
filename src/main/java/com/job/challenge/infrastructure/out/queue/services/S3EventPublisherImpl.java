package com.job.challenge.infrastructure.out.queue.services;

import com.job.challenge.application.domain.S3Event;
import com.job.challenge.infrastructure.out.queue.dto.Message;
import com.job.challenge.infrastructure.out.queue.mappers.MessageMapper;
import com.job.challenge.interfaces.out.S3EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class S3EventPublisherImpl implements S3EventPublisher {

    private final AWSSqsService awsSqsService;

    @Autowired
    public S3EventPublisherImpl(AWSSqsService awsSqsService) {
        this.awsSqsService = awsSqsService;
    }

    @Override
    public Mono<Void> publish(S3Event event) {
        Message message = MessageMapper.toMessage(event);
        return awsSqsService.publish(message);
    }
}
