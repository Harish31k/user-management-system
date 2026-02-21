package com.management.mapper;

import com.management.dto.request.RegisterRequest;
import com.management.dto.response.UserProfileResponse;
import com.management.entity.Role;
import com.management.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request, String encodedPassword, Set<Role> roles) {

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setRoles(roles);
        return user;
    }

    public UserProfileResponse toResponse(User user) {

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet())
                )
                .build();
    }
}