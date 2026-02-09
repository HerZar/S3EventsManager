package com.job.challenge.infrastructure.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class S3EventDto {

    @JsonProperty("id")
    private String id;
    @JsonProperty("bucketName")
    private String bucketName;
    @JsonProperty("objectKey")
    private String objectKey;
    @JsonProperty("eventType")
    private EventTypeDto type;
    @JsonProperty("eventTime")
    private LocalDateTime time;
    @JsonProperty("objectSize")
    private Integer objectSize;
}
