package com.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
}