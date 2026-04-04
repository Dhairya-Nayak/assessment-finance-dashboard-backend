package com.financedashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financedashboard.dto.response.PagedResponse;
import com.financedashboard.entity.AuditLog;
import com.financedashboard.repository.AuditLogRepository;
import com.financedashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String entityType, Long entityId, AuditLog.AuditAction action,
                          Long userId, Object oldValues, Object newValues, String description) {
        try {
            String username = userId != null ?
                    userRepository.findById(userId).map(u -> u.getUsername()).orElse("unknown") : "system";

            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .userId(userId)
                    .username(username)
                    .oldValues(oldValues != null ? toJson(oldValues) : null)
                    .newValues(newValues != null ? toJson(newValues) : null)
                    .ipAddress(getClientIpAddress())
                    .userAgent(getUserAgent())
                    .description(description)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} {} on {} #{}", action, username, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    public void logLogin(Long userId, String username) {
        logAction("USER", userId, AuditLog.AuditAction.LOGIN, userId, null, null,
                "User logged in: " + username);
    }

    public void logLogout(Long userId) {
        logAction("USER", userId, AuditLog.AuditAction.LOGOUT, userId, null, null,
                "User logged out");
    }

    public void logCreate(String entityType, Long entityId, Long userId, Object newValues) {
        logAction(entityType, entityId, AuditLog.AuditAction.CREATE, userId, null, newValues,
                entityType + " created: #" + entityId);
    }

    public void logUpdate(String entityType, Long entityId, Long userId, Object oldValues, Object newValues) {
        logAction(entityType, entityId, AuditLog.AuditAction.UPDATE, userId, oldValues, newValues,
                entityType + " updated: #" + entityId);
    }

    public void logDelete(String entityType, Long entityId, Long userId) {
        logAction(entityType, entityId, AuditLog.AuditAction.DELETE, userId, null, null,
                entityType + " deleted: #" + entityId);
    }

    public void logExport(String entityType, Long userId, Map<String, Object> details) {
        logAction(entityType, 0L, AuditLog.AuditAction.EXPORT, userId, null, details,
                "Data exported: " + entityType);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLog> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> logsPage = auditLogRepository.findAll(pageable);
        return PagedResponse.from(logsPage);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLog> getAuditLogsByEntity(String entityType, Long entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> logsPage = auditLogRepository.findAllByEntityTypeAndEntityId(entityType, entityId, pageable);
        return PagedResponse.from(logsPage);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLog> getAuditLogsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> logsPage = auditLogRepository.findAllByUserId(userId, pageable);
        return PagedResponse.from(logsPage);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> logsPage = auditLogRepository.findByDateRange(start, end, pageable);
        return PagedResponse.from(logsPage);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLog> getUserLoginHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logsPage = auditLogRepository.findLoginHistoryByUserId(userId, pageable);
        return PagedResponse.from(logsPage);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getActionCounts(LocalDateTime since) {
        return auditLogRepository.countActionsSince(since);
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            return "{}";
        }
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not get client IP address", e);
        }
        return null;
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not get user agent", e);
        }
        return null;
    }
}
