package com.job.challenge.infrastructure.out.queue.mappers;

import com.job.challenge.application.domain.S3Event;
import com.job.challenge.infrastructure.out.queue.dto.Message;

public class MessageMapper {

    public static Message toMessage(S3Event s3Event) {
        return Message.builder()
                .bucketName(s3Event.getBucketName())
                .objectKey(s3Event.getObjectKey())
                .eventType(s3Event.getType().name())
                .timestamp(s3Event.getTime())
                .objectSize(s3Event.getObjectSize())
                .build();
    }
}
