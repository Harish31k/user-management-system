package com.management.service;

public interface AuditService {
    void log(String action, Long userId);
}
