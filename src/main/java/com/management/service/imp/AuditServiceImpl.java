package com.management.service.imp;

import com.management.entity.AuditLog;
import com.management.repository.AuditLogRepository;
import com.management.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(String action, Long userId) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }
}
