package com.financedashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_logs_action", columnList = "action"),
        @Index(name = "idx_audit_logs_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private AuditAction action;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "old_values", columnDefinition = "JSON")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "JSON")
    private String newValues;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum AuditAction {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
        EXPORT
    }
}
