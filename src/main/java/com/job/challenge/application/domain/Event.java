package com.job.challenge.application.domain;

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
public class Event {

    private String id;
    private String bucketName;
    private String objectKey;
    private EventType type;
    private LocalDateTime time;
    private Integer objectSize;
}
