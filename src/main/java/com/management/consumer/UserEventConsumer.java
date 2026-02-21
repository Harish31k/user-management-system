package com.management.consumer;

import com.management.dto.event.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserEventConsumer {

    @KafkaListener(
            topics = "user-events",
            groupId = "user-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(UserEvent event) {
        log.info("Received Kafka Event → Type: {}, Email: {}, Time: {}",
                event.getEventType(),
                event.getEmail(),
                event.getTimestamp());
    }
}
