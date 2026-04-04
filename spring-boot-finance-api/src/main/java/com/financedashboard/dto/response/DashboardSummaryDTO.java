package com.financedashboard.dto.response;

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
public class DashboardSummaryDTO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private BigDecimal savingsRate;
    private Long totalTransactions;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    private List<CategorySummary> incomeByCategory;
    private List<CategorySummary> expenseByCategory;
    private List<MonthlyTrend> monthlyTrends;
    private List<FinancialRecordDTO> recentTransactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private Long categoryId;
        private String categoryName;
        private String categoryColor;
        private String categoryIcon;
        private BigDecimal amount;
        private Long transactionCount;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private Integer year;
        private Integer month;
        private String monthName;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal net;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyTrend {
        private String weekKey;
        private LocalDate weekStart;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal net;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySummary {
        private LocalDate date;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal balance;
    }
}
