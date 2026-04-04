package com.financedashboard.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_records", indexes = {
        @Index(name = "idx_financial_records_user_id", columnList = "user_id"),
        @Index(name = "idx_financial_records_category_id", columnList = "category_id"),
        @Index(name = "idx_financial_records_type", columnList = "type"),
        @Index(name = "idx_financial_records_transaction_date", columnList = "transaction_date"),
        @Index(name = "idx_financial_records_status", columnList = "status"),
        @Index(name = "idx_financial_records_user_date", columnList = "user_id, transaction_date"),
        @Index(name = "idx_financial_records_user_type_date", columnList = "user_id, type, transaction_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private RecordType type;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 50)
    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Size(max = 255)
    @Column(name = "tags", length = 255)
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private RecordStatus status = RecordStatus.CONFIRMED;

    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurring_frequency", length = 20)
    private RecurringFrequency recurringFrequency;

    @Size(max = 500)
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "last_modified_by")
    private Long lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum RecordType {
        INCOME,
        EXPENSE
    }

    public enum RecordStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        DELETED
    }

    public enum RecurringFrequency {
        DAILY,
        WEEKLY,
        BIWEEKLY,
        MONTHLY,
        QUARTERLY,
        YEARLY
    }
}
