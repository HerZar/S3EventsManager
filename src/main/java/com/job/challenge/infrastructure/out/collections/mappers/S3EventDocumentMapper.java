package com.job.challenge.infrastructure.out.collections.mappers;

import com.job.challenge.application.domain.EventType;
import com.job.challenge.application.domain.S3Event;
import com.job.challenge.infrastructure.out.collections.documents.EventTypeDocument;
import com.job.challenge.infrastructure.out.collections.documents.S3EventDocument;
import org.bson.types.ObjectId;

public class S3EventDocumentMapper {

    public static S3EventDocument toS3EventDocument(S3Event event) {
        return S3EventDocument.builder()
                .id(event.getId() == null ? null : new ObjectId(event.getId()))
                .bucketName(event.getBucketName())
                .objectKey(event.getObjectKey())
                .type(EventTypeDocument.valueOf(event.getType().name()))
                .time(event.getTime())
                .objectSize(event.getObjectSize()).build();
    }

    public static S3Event toS3Event(S3EventDocument document) {
        return S3Event.builder()
                .id(document.getId() == null ? null : document.getId().toString())
                .bucketName(document.getBucketName())
                .objectKey(document.getObjectKey())
                .type(EventType.valueOf(document.getType().name()))
                .time(document.getTime())
                .objectSize(document.getObjectSize()).build();
    }
}
