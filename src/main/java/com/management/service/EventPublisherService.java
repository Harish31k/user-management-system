package com.management.service;

import com.management.dto.event.UserEvent;

public interface EventPublisherService {
    void publish(UserEvent event);
}
