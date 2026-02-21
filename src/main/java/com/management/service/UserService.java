package com.management.service;

import com.management.dto.response.UserProfileResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    @Cacheable(value = "userProfile", key = "#email")
    UserProfileResponse getCurrentUserProfile(String email);

    void updateLastLogin(String email);

    long getTotalUsers();
}