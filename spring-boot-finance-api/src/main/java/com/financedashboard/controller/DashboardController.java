package com.financedashboard.controller;

import com.financedashboard.dto.response.ApiResponse;
import com.financedashboard.dto.response.DashboardSummaryDTO;
import com.financedashboard.entity.FinancialRecord;
import com.financedashboard.security.CurrentUser;
import com.financedashboard.security.UserPrincipal;
import com.financedashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @CurrentUser UserPrincipal currentUser) {
        
        // Default to current month if dates not provided
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.debug("Getting dashboard summary for user {} from {} to {}", 
                currentUser.getUsername(), startDate, endDate);
        
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary(
                currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/quick")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getQuickSummary(
            @CurrentUser UserPrincipal currentUser) {
        
        DashboardSummaryDTO summary = dashboardService.getQuickSummary(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/analytics/category-breakdown")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DashboardSummaryDTO.CategorySummary>>> getCategoryBreakdown(
            @RequestParam FinancialRecord.RecordType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @CurrentUser UserPrincipal currentUser) {
        
        log.debug("Getting category breakdown for user {} type {} from {} to {}", 
                currentUser.getUsername(), type, startDate, endDate);
        
        List<DashboardSummaryDTO.CategorySummary> breakdown = dashboardService.getCategorySummary(
                currentUser.getId(), type, startDate, endDate, null);
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }

    @GetMapping("/analytics/monthly-trends")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DashboardSummaryDTO.MonthlyTrend>>> getMonthlyTrends(
            @RequestParam(defaultValue = "12") int months,
            @CurrentUser UserPrincipal currentUser) {
        
        log.debug("Getting monthly trends for user {} for last {} months", 
                currentUser.getUsername(), months);
        
        List<DashboardSummaryDTO.MonthlyTrend> trends = dashboardService.getMonthlyTrends(
                currentUser.getId(), months);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }

    @GetMapping("/analytics/weekly-trends")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DashboardSummaryDTO.WeeklyTrend>>> getWeeklyTrends(
            @RequestParam(defaultValue = "12") int weeks,
            @CurrentUser UserPrincipal currentUser) {
        
        log.debug("Getting weekly trends for user {} for last {} weeks", 
                currentUser.getUsername(), weeks);
        
        List<DashboardSummaryDTO.WeeklyTrend> trends = dashboardService.getWeeklyTrends(
                currentUser.getId(), weeks);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }

    @GetMapping("/analytics/daily-summary")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DashboardSummaryDTO.DailySummary>>> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @CurrentUser UserPrincipal currentUser) {
        
        log.debug("Getting daily summary for user {} from {} to {}", 
                currentUser.getUsername(), startDate, endDate);
        
        List<DashboardSummaryDTO.DailySummary> summary = dashboardService.getDailySummary(
                currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/analytics/top-categories")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DashboardSummaryDTO.CategorySummary>>> getTopCategories(
            @RequestParam FinancialRecord.RecordType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "5") int limit,
            @CurrentUser UserPrincipal currentUser) {
        
        log.debug("Getting top {} categories for user {} type {} from {} to {}", 
                limit, currentUser.getUsername(), type, startDate, endDate);
        
        List<DashboardSummaryDTO.CategorySummary> topCategories = dashboardService.getTopCategories(
                currentUser.getId(), type, startDate, endDate, limit);
        return ResponseEntity.ok(ApiResponse.success(topCategories));
    }

    @GetMapping("/user/{userId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getUserDashboardSummary(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.debug("Admin getting dashboard summary for user {} from {} to {}", userId, startDate, endDate);
        
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
