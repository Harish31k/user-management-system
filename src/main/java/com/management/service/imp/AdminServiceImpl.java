package com.management.service.imp;

import com.management.dto.response.AdminStatsResponse;
import com.management.entity.User;
import com.management.repository.UserRepository;
import com.management.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public AdminStatsResponse getStats() {

        List<User> users = userRepository.findAll();

        return AdminStatsResponse.builder()
                .totalUsers(users.size())
                .recentLogins(
                        users.stream()
                                .map(user ->
                                        AdminStatsResponse.UserLoginInfo.builder()
                                                .userId(user.getId())
                                                .email(user.getEmail())
                                                .lastLogin(user.getLastLogin())
                                                .build()
                                )
                                .toList()
                )
                .build();
    }
}