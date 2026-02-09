package com.job.challenge.infrastructure.in.rest.controllers;


import com.job.challenge.infrastructure.in.rest.dto.S3EventCreateResponse;
import com.job.challenge.infrastructure.in.rest.dto.S3EventDto;
import com.job.challenge.infrastructure.in.rest.mappers.GetEventsRequestMapper;
import com.job.challenge.infrastructure.in.rest.mappers.S3EventMapper;
import com.job.challenge.interfaces.in.S3EventService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/s3-events")
public class S3EventController {

    private S3EventService service;

    @GetMapping("/{bucketName}")
    public Flux<S3EventDto> get(@PathVariable String bucketName,
                                @RequestParam(defaultValue = "0") Integer page,
                                @RequestParam(defaultValue = "10") Integer size) {
        var request = GetEventsRequestMapper.toGetEventsRequest(page, size);
        return service.get(bucketName, request)
                .map(S3EventMapper::toS3EventDto);
    }

    @PostMapping
    public Mono<S3EventCreateResponse> create(@RequestBody S3EventDto event) {
        return service.create(S3EventMapper.toS3Event(event)).map(result -> S3EventCreateResponse.builder().id(result).build());
    }
}
