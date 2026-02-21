package com.management.service.imp;

import com.management.dto.event.UserEvent;
import com.management.service.EventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherServiceImpl implements EventPublisherService {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    private static final String TOPIC = "user-events";

    @Override
    public void publish(UserEvent event) {

        kafkaTemplate.send(TOPIC, event.getEmail(), event);

        log.info(" Published Kafka Event → Type: {}, Email: {}",
                event.getEventType(),
                event.getEmail());
    }
}