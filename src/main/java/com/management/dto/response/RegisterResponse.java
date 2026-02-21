package com.management.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {

    private Long id;
    private String username;
    private String email;
    private String message;
}