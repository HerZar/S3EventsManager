package com.job.challenge.infrastructure.in.rest.mappers;

import com.job.challenge.application.domain.GetEventsRequest;
import com.job.challenge.infrastructure.in.rest.dto.GetEventsRequestDto;

public class GetEventsRequestMapper {
    
    public static GetEventsRequest toGetEventsRequest(Integer page, Integer size) {
        return com.job.challenge.application.domain.GetEventsRequest.of(page, size);
    }
    
    public static GetEventsRequest toGetEventsRequest(GetEventsRequestDto dto) {
        return GetEventsRequest.of(dto.getPage(), dto.getSize());
    }
}
