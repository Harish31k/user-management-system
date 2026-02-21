package com.management;

import com.management.dto.response.UserProfileResponse;
import com.management.entity.User;
import com.management.exception.ResourceNotFoundException;
import com.management.mapper.UserMapper;
import com.management.repository.UserRepository;
import com.management.service.imp.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getCurrentUserProfile_ShouldReturnProfile_WhenUserExists() {

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .username("test")
                .build();

        UserProfileResponse responseDto = UserProfileResponse.builder()
                .email("test@gmail.com")
                .username("test")
                .build();

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(responseDto);

        UserProfileResponse response =
                userService.getCurrentUserProfile("test@gmail.com");

        assertEquals("test@gmail.com", response.getEmail());
        assertEquals("test", response.getUsername());
    }

    @Test
    void getCurrentUserProfile_ShouldThrowException_WhenNotFound() {

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getCurrentUserProfile("test@gmail.com"));
    }
}