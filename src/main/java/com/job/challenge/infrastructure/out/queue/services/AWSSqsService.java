package com.job.challenge.infrastructure.out.queue.services;

import com.job.challenge.application.exceptions.ConflictException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.job.challenge.infrastructure.out.queue.dto.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;

@Service
public class AWSSqsService {

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final ObjectMapper objectMapper;

    public AWSSqsService(@Value("${aws.sqs.queue-url}") String queueUrl,
                         @Value("${aws.access-key}") String accessKey,
                         @Value("${aws.secret-key}") String secretKey,
                         @Value("${aws.region}") String region) {
        this.queueUrl = queueUrl;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.sqsClient = SqsClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .build();
    }

    public Mono<Void> publish(Message message) {
        return Mono.fromRunnable(() -> {
            try {
                String messageBody = objectMapper.writeValueAsString(message);
                SendMessageRequest request = SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messageBody)
                        .build();
                
                sqsClient.sendMessage(request);
            } catch (Exception e) {
                throw new ConflictException("Failed to publish message to SQS: " + e.getMessage(), "MESSAGE_PUBLISHING_ERROR");
            }
        })
        .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> 
                    new ConflictException("Failed to publish message to SQS after 3 attempts", "MESSAGE_PUBLISHING_RETRY_EXHAUSTED")))
        .then();
    }
}
