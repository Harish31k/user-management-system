package com.management.dto.response;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Builder
@Data
@AllArgsConstructor
public class AdminStatsResponse {

    private long totalUsers;
    private List<UserLoginInfo> recentLogins;

    @Builder
    @Data
    @AllArgsConstructor
    public static class UserLoginInfo {
        private Long userId;
        private String email;
        private LocalDateTime lastLogin;
    }
}