package com.job.challenge.infrastructure.out.collections.services.strategy;

import com.job.challenge.infrastructure.out.collections.documents.EventTypeDocument;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExistenceStrategyFactory {
    
    private final Map<EventTypeDocument, ExistenceStrategy> strategies;
    
    public ExistenceStrategyFactory() {
        this.strategies = Map.of(
            EventTypeDocument.OBJECT_UPDATED, new UpdatedExistenceStrategy(),
            EventTypeDocument.OBJECT_CREATED, new DefaultExistenceStrategy(),
            EventTypeDocument.OBJECT_DELETED, new DefaultExistenceStrategy()
        );
    }
    
    public ExistenceStrategy getStrategy(EventTypeDocument type) {
        return strategies.getOrDefault(type, new DefaultExistenceStrategy());
    }
}
