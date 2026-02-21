package com.management;

import com.management.dto.response.AdminStatsResponse;
import com.management.entity.User;
import com.management.repository.UserRepository;
import com.management.service.imp.AdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void getStats_ShouldReturnCorrectCounts() {

        User user1 = User.builder()
                .id(1L)
                .email("user1@gmail.com")
                .lastLogin(LocalDateTime.now())
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("user2@gmail.com")
                .lastLogin(LocalDateTime.now())
                .build();

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        AdminStatsResponse response = adminService.getStats();

        assertEquals(2, response.getTotalUsers());
        assertEquals(2, response.getRecentLogins().size());

        verify(userRepository).findAll();
    }
}