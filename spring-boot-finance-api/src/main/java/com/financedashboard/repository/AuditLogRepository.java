package com.financedashboard.repository;

import com.financedashboard.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findAllByUserId(Long userId, Pageable pageable);

    Page<AuditLog> findAllByAction(AuditLog.AuditAction action, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    Page<AuditLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType " +
           "AND al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findByEntityTypeAndDateRange(
            @Param("entityType") String entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al.action, COUNT(al) FROM AuditLog al " +
           "WHERE al.createdAt >= :since GROUP BY al.action")
    List<Object[]> countActionsSince(@Param("since") LocalDateTime since);

    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId " +
           "AND al.action IN ('LOGIN', 'LOGOUT') ORDER BY al.createdAt DESC")
    Page<AuditLog> findLoginHistoryByUserId(@Param("userId") Long userId, Pageable pageable);
}
