package com.management.service.imp;

import com.management.dto.response.UserProfileResponse;
import com.management.entity.User;
import com.management.exception.ResourceNotFoundException;
import com.management.mapper.UserMapper;
import com.management.repository.UserRepository;
import com.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // REQUIRED for Spring Security Authentication

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getRoles()
                        .stream()
                        // IMPORTANT: role.getName() already contains ROLE_
                        .map(role ->
                                new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toSet())
        );
    }

    // Cached user profile (/api/users/me)

    @Override
    @Cacheable(value = "currentUser", key = "#email")
    public UserProfileResponse getCurrentUserProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        //  Use mapper instead of manual DTO building
        return userMapper.toResponse(user);
    }

    // Update last login time
    @Override
    @CacheEvict(value = "currentUser", key = "#email")
    public void updateLastLogin(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    //Admin stats method

    @Override
    public long getTotalUsers() {
        return userRepository.count();
    }
}