package com.financedashboard.controller;

import com.financedashboard.dto.response.ApiResponse;
import com.financedashboard.dto.response.PagedResponse;
import com.financedashboard.entity.AuditLog;
import com.financedashboard.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        PagedResponse<AuditLog> logs = auditService.getAuditLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        PagedResponse<AuditLog> logs = auditService.getAuditLogsByEntity(entityType, entityId, page, size);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        PagedResponse<AuditLog> logs = auditService.getAuditLogsByUser(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/user/{userId}/login-history")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getUserLoginHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PagedResponse<AuditLog> logs = auditService.getUserLoginHistory(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        PagedResponse<AuditLog> logs = auditService.getAuditLogsByDateRange(startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getAuditStats(
            @RequestParam(defaultValue = "24") int hoursBack) {
        
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        List<Object[]> results = auditService.getActionCounts(since);
        
        Map<String, Long> stats = results.stream()
                .collect(Collectors.toMap(
                        row -> ((AuditLog.AuditAction) row[0]).name(),
                        row -> ((Number) row[1]).longValue()
                ));
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
