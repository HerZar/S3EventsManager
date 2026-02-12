package com.job.challenge.infrastructure.out.queue.mappers;

import com.job.challenge.application.domain.S3Event;
import com.job.challenge.infrastructure.out.queue.dto.Message;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class MessageMapper {

    public static Message toMessage(S3Event s3Event) {
        String formattedDate = s3Event.getTime()
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
        
        return Message.builder()
                .eventId(s3Event.getId())
                .bucketName(s3Event.getBucketName())
                .objectKey(s3Event.getObjectKey())
                .eventTime(formattedDate)
                .build();
    }
}
