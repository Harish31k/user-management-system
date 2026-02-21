package com.management.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;

    // Single error
    private String error;

    // Field-wise validation errors
    private Map<String, String> validationErrors;
}