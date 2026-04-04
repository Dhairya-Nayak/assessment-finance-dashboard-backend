package com.financedashboard.dto.request;

import com.financedashboard.entity.FinancialRecord;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecordRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Type is required")
    private FinancialRecord.RecordType type;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 50, message = "Reference number must not exceed 50 characters")
    private String referenceNumber;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate transactionDate;

    private String notes;

    @Size(max = 255, message = "Tags must not exceed 255 characters")
    private String tags;

    private Boolean isRecurring;

    private FinancialRecord.RecurringFrequency recurringFrequency;

    @Size(max = 500, message = "Attachment URL must not exceed 500 characters")
    private String attachmentUrl;
}
