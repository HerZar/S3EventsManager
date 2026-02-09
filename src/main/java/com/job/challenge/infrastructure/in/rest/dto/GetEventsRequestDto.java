package com.job.challenge.infrastructure.in.rest.dto;

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
public class GetEventsRequestDto {
    
    private Integer page;
    
    private Integer size;
    
    public static GetEventsRequestDto of(Integer page, Integer size) {
        return GetEventsRequestDto.builder()
                .page(page != null && page >= 0 ? page : 0)
                .size(size != null && size > 0 ? size : 10)
                .build();
    }
}
