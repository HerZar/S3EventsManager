package com.job.challenge.infrastructure.out.queue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("bucketName")
    private String bucketName;

    @JsonProperty("objectKey")
    private String objectKey;

    @JsonProperty("eventTime")
    private String eventTime;
}
