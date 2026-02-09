package com.job.challenge.application.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetEventsRequest {
    
    private Integer page;
    
    private Integer size;
    
    public static GetEventsRequest of(Integer page, Integer size) {
        return GetEventsRequest.builder()
                .page(page != null && page >= 0 ? page : 0)
                .size(size != null && size > 0 ? size : 10)
                .build();
    }
}
