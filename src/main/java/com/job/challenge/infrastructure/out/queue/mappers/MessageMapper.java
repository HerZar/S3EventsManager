package com.job.challenge.infrastructure.out.queue.mappers;

import com.job.challenge.application.domain.Event;
import com.job.challenge.infrastructure.out.queue.dto.Message;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class MessageMapper {

    public static Message toMessage(Event event) {
        String formattedDate = event.getTime()
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
        
        return Message.builder()
                .eventId(event.getId())
                .bucketName(event.getBucketName())
                .objectKey(event.getObjectKey())
                .eventTime(formattedDate)
                .build();
    }
}
