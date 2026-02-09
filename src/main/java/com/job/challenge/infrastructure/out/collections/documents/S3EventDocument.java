package com.job.challenge.infrastructure.out.collections.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "s3_events")
public class S3EventDocument {

    @Id
    private ObjectId id;
    private String bucketName;
    private String objectKey;
    private EventTypeDocument type;
    private LocalDateTime time;
    private Integer objectSize;
}
