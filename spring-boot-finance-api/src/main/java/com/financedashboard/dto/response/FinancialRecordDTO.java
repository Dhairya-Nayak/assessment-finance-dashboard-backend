package com.financedashboard.dto.response;

import com.financedashboard.entity.FinancialRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecordDTO {

    private Long id;
    private BigDecimal amount;
    private FinancialRecord.RecordType type;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private String categoryIcon;
    private Long userId;
    private String username;
    private String description;
    private String referenceNumber;
    private LocalDate transactionDate;
    private String notes;
    private String tags;
    private FinancialRecord.RecordStatus status;
    private Boolean isRecurring;
    private FinancialRecord.RecurringFrequency recurringFrequency;
    private String attachmentUrl;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long lastModifiedBy;

    public static FinancialRecordDTO fromEntity(FinancialRecord record) {
        return FinancialRecordDTO.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .categoryId(record.getCategory().getId())
                .categoryName(record.getCategory().getName())
                .categoryColor(record.getCategory().getColor())
                .categoryIcon(record.getCategory().getIcon())
                .userId(record.getUser().getId())
                .username(record.getUser().getUsername())
                .description(record.getDescription())
                .referenceNumber(record.getReferenceNumber())
                .transactionDate(record.getTransactionDate())
                .notes(record.getNotes())
                .tags(record.getTags())
                .status(record.getStatus())
                .isRecurring(record.getIsRecurring())
                .recurringFrequency(record.getRecurringFrequency())
                .attachmentUrl(record.getAttachmentUrl())
                .version(record.getVersion())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .createdBy(record.getCreatedBy())
                .lastModifiedBy(record.getLastModifiedBy())
                .build();
    }
}
