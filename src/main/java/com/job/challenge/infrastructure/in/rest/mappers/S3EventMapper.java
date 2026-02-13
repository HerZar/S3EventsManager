package com.job.challenge.infrastructure.in.rest.mappers;

import com.job.challenge.application.domain.EventType;
import com.job.challenge.application.domain.Event;
import com.job.challenge.infrastructure.in.rest.dto.EventTypeDto;
import com.job.challenge.infrastructure.in.rest.dto.S3EventDto;
import reactor.core.publisher.Mono;

public class S3EventMapper {

    public static Event toS3Event(S3EventDto event) {
        return Event.builder()
                .id(event.getId())
                .bucketName(event.getBucketName())
                .objectKey(event.getObjectKey())
                .type(EventType.valueOf(event.getType().name()))
                .time(event.getTime())
                .objectSize(event.getObjectSize()).build();
    }

    public static S3EventDto toS3EventDto(Event event) {
        return S3EventDto.builder()
                .id(event.getId())
                .bucketName(event.getBucketName())
                .objectKey(event.getObjectKey())
                .type(EventTypeDto.valueOf(event.getType().name()))
                .time(event.getTime())
                .objectSize(event.getObjectSize()).build();
    }

    public static Mono<S3EventDto> toS3EventDto(Mono<Event> event) {
        return event.map(s3Event -> S3EventDto.builder()
                .bucketName(s3Event.getBucketName())
                .objectKey(s3Event.getObjectKey())
                .type(EventTypeDto.valueOf(s3Event.getType().name()))
                .time(s3Event.getTime())
                .objectSize(s3Event.getObjectSize()).build());
    }
}
