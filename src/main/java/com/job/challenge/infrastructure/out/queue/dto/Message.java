package com.job.challenge.infrastructure.out.queue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @JsonProperty("bucketName")
    private String bucketName;

    @JsonProperty("objectKey")
    private String objectKey;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("objectSize")
    private Integer objectSize;
}
