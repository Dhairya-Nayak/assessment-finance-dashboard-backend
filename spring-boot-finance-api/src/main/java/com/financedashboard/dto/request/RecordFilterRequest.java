package com.financedashboard.dto.request;

import com.financedashboard.entity.FinancialRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordFilterRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private FinancialRecord.RecordType type;
    private List<Long> categoryIds;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String search;
    private FinancialRecord.RecordStatus status;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "transactionDate";

    @Builder.Default
    private String sortDirection = "DESC";
}
