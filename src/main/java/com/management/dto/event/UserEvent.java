package com.management.dto.event;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEvent {

    private Long userId;
    private String email;
    private String eventType; // REGISTERED, LOGIN
    private LocalDateTime timestamp;
}