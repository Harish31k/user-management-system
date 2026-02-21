package com.management.service.imp;

import com.management.dto.event.UserEvent;
import com.management.dto.request.LoginRequest;
import com.management.dto.request.RegisterRequest;
import com.management.dto.response.AuthResponse;
import com.management.dto.response.RegisterResponse;
import com.management.entity.Role;
import com.management.entity.User;
import com.management.exception.EmailAlreadyExistsException;
import com.management.exception.InvalidCredentialsException;
import com.management.exception.ResourceNotFoundException;
import com.management.mapper.UserMapper;
import com.management.repository.RoleRepository;
import com.management.repository.UserRepository;
import com.management.config.JwtService;
import com.management.service.AuthService;
import com.management.service.AuditService;
import com.management.service.EventPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final EventPublisherService eventPublisherService;
    private final AuditService auditService;

    //Register user

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = userMapper.toEntity(
                request,
                encodedPassword,
                Set.of(userRole)
        );

        User savedUser = userRepository.save(user);

        //  Audit Log for Registration
        auditService.log("REGISTERED", savedUser.getId());

        // Publish REGISTER event to Kafka
        UserEvent event = UserEvent.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .eventType("REGISTERED")
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisherService.publish(event);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .message("User registered successfully. Please login.")
                .build();
    }

    //Login user

    @Override
    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        //  Audit Log for Login
        auditService.log("LOGIN", user.getId());

        // Publish LOGIN event to Kafka
        UserEvent event = UserEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .eventType("LOGIN")
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisherService.publish(event);

        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}