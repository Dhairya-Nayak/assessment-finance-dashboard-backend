package com.financedashboard.service;

import com.financedashboard.dto.response.DashboardSummaryDTO;
import com.financedashboard.dto.response.FinancialRecordDTO;
import com.financedashboard.entity.FinancialRecord;
import com.financedashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "dashboardSummary", key = "#userId + '-' + #startDate + '-' + #endDate")
    public DashboardSummaryDTO getDashboardSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching dashboard summary for user {} from {} to {}", userId, startDate, endDate);

        // Get totals
        BigDecimal totalIncome = recordRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, FinancialRecord.RecordType.INCOME, startDate, endDate);
        BigDecimal totalExpense = recordRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, FinancialRecord.RecordType.EXPENSE, startDate, endDate);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpense);
        
        // Calculate savings rate
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netBalance.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Get transaction count
        long totalTransactions = recordRepository.countByUserId(userId);

        // Get category summaries
        List<DashboardSummaryDTO.CategorySummary> incomeByCategory = getCategorySummary(
                userId, FinancialRecord.RecordType.INCOME, startDate, endDate, totalIncome);
        List<DashboardSummaryDTO.CategorySummary> expenseByCategory = getCategorySummary(
                userId, FinancialRecord.RecordType.EXPENSE, startDate, endDate, totalExpense);

        // Get monthly trends (last 12 months)
        List<DashboardSummaryDTO.MonthlyTrend> monthlyTrends = getMonthlyTrends(userId, 12);

        // Get recent transactions
        List<FinancialRecordDTO> recentTransactions = recordRepository
                .findRecentTransactions(userId, PageRequest.of(0, 10))
                .getContent().stream()
                .map(FinancialRecordDTO::fromEntity)
                .collect(Collectors.toList());

        return DashboardSummaryDTO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .savingsRate(savingsRate)
                .totalTransactions(totalTransactions)
                .periodStart(startDate)
                .periodEnd(endDate)
                .incomeByCategory(incomeByCategory)
                .expenseByCategory(expenseByCategory)
                .monthlyTrends(monthlyTrends)
                .recentTransactions(recentTransactions)
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categorySummary", key = "#userId + '-' + #type + '-' + #startDate + '-' + #endDate")
    public List<DashboardSummaryDTO.CategorySummary> getCategorySummary(
            Long userId, FinancialRecord.RecordType type, LocalDate startDate, LocalDate endDate, BigDecimal total) {
        
        List<Object[]> results = recordRepository.getCategoryWiseSummary(userId, type, startDate, endDate);
        List<DashboardSummaryDTO.CategorySummary> summaries = new ArrayList<>();

        for (Object[] row : results) {
            Long categoryId = ((Number) row[0]).longValue();
            String categoryName = (String) row[1];
            String categoryColor = (String) row[2];
            BigDecimal amount = (BigDecimal) row[3];
            Long transactionCount = ((Number) row[4]).longValue();

            BigDecimal percentage = BigDecimal.ZERO;
            if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            summaries.add(DashboardSummaryDTO.CategorySummary.builder()
                    .categoryId(categoryId)
                    .categoryName(categoryName)
                    .categoryColor(categoryColor)
                    .amount(amount)
                    .transactionCount(transactionCount)
                    .percentage(percentage)
                    .build());
        }

        return summaries;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "monthlyTrends", key = "#userId + '-' + #months")
    public List<DashboardSummaryDTO.MonthlyTrend> getMonthlyTrends(Long userId, int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);
        List<Object[]> results = recordRepository.getMonthlyTrends(userId, startDate);
        List<DashboardSummaryDTO.MonthlyTrend> trends = new ArrayList<>();

        for (Object[] row : results) {
            Integer year = ((Number) row[0]).intValue();
            Integer month = ((Number) row[1]).intValue();
            BigDecimal income = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            BigDecimal expense = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;

            String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            trends.add(DashboardSummaryDTO.MonthlyTrend.builder()
                    .year(year)
                    .month(month)
                    .monthName(monthName)
                    .income(income)
                    .expense(expense)
                    .net(income.subtract(expense))
                    .build());
        }

        return trends;
    }

    @Transactional(readOnly = true)
    public List<DashboardSummaryDTO.WeeklyTrend> getWeeklyTrends(Long userId, int weeks) {
        LocalDate startDate = LocalDate.now().minusWeeks(weeks);
        List<Object[]> results = recordRepository.getWeeklyTrends(userId, startDate);
        List<DashboardSummaryDTO.WeeklyTrend> trends = new ArrayList<>();

        for (Object[] row : results) {
            String weekKey = String.valueOf(row[0]);
            LocalDate weekStart = ((java.sql.Date) row[1]).toLocalDate();
            BigDecimal income = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            BigDecimal expense = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;

            trends.add(DashboardSummaryDTO.WeeklyTrend.builder()
                    .weekKey(weekKey)
                    .weekStart(weekStart)
                    .income(income)
                    .expense(expense)
                    .net(income.subtract(expense))
                    .build());
        }

        return trends;
    }

    @Transactional(readOnly = true)
    public List<DashboardSummaryDTO.DailySummary> getDailySummary(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = recordRepository.getDailySummary(userId, startDate, endDate);
        List<DashboardSummaryDTO.DailySummary> summaries = new ArrayList<>();

        for (Object[] row : results) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            BigDecimal income = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            BigDecimal expense = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

            summaries.add(DashboardSummaryDTO.DailySummary.builder()
                    .date(date)
                    .income(income)
                    .expense(expense)
                    .balance(income.subtract(expense))
                    .build());
        }

        return summaries;
    }

    @Transactional(readOnly = true)
    public List<DashboardSummaryDTO.CategorySummary> getTopCategories(
            Long userId, FinancialRecord.RecordType type, LocalDate startDate, LocalDate endDate, int limit) {
        
        List<Object[]> results = recordRepository.getTopCategories(
                userId, type, startDate, endDate, PageRequest.of(0, limit));
        
        List<DashboardSummaryDTO.CategorySummary> summaries = new ArrayList<>();

        for (Object[] row : results) {
            Long categoryId = ((Number) row[0]).longValue();
            String categoryName = (String) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            summaries.add(DashboardSummaryDTO.CategorySummary.builder()
                    .categoryId(categoryId)
                    .categoryName(categoryName)
                    .amount(amount)
                    .build());
        }

        return summaries;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getQuickSummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        BigDecimal totalIncome = recordRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, FinancialRecord.RecordType.INCOME, startOfMonth, today);
        BigDecimal totalExpense = recordRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, FinancialRecord.RecordType.EXPENSE, startOfMonth, today);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        return DashboardSummaryDTO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(totalIncome.subtract(totalExpense))
                .periodStart(startOfMonth)
                .periodEnd(today)
                .build();
    }
}
