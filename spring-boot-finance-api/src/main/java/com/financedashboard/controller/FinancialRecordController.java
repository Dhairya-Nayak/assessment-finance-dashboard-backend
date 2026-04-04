package com.financedashboard.controller;

import com.financedashboard.dto.request.FinancialRecordRequest;
import com.financedashboard.dto.request.RecordFilterRequest;
import com.financedashboard.dto.response.ApiResponse;
import com.financedashboard.dto.response.FinancialRecordDTO;
import com.financedashboard.dto.response.PagedResponse;
import com.financedashboard.entity.FinancialRecord;
import com.financedashboard.security.CurrentUser;
import com.financedashboard.security.UserPrincipal;
import com.financedashboard.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<FinancialRecordDTO>>> getRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) FinancialRecord.RecordType type,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) FinancialRecord.RecordStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @CurrentUser UserPrincipal currentUser) {

        RecordFilterRequest filter = RecordFilterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .type(type)
                .categoryIds(categoryIds)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .search(search)
                .status(status)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        PagedResponse<FinancialRecordDTO> records = recordService.getRecords(filter, currentUser);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialRecordDTO>> getRecordById(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        FinancialRecordDTO record = recordService.getRecordById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<PagedResponse<FinancialRecordDTO>>> getUserRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        PagedResponse<FinancialRecordDTO> records = recordService.getUserRecords(userId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/my-records")
    public ResponseEntity<ApiResponse<PagedResponse<FinancialRecordDTO>>> getMyRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal currentUser) {
        
        PagedResponse<FinancialRecordDTO> records = recordService.getUserRecords(
                currentUser.getId(), page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<FinancialRecordDTO>>> getRecordsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @CurrentUser UserPrincipal currentUser) {
        
        List<FinancialRecordDTO> records = recordService.getRecordsByDateRange(
                currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<FinancialRecordDTO>>> searchRecords(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser UserPrincipal currentUser) {
        
        PagedResponse<FinancialRecordDTO> records = recordService.searchRecords(
                currentUser.getId(), query, page, size);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<FinancialRecordDTO>>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit,
            @CurrentUser UserPrincipal currentUser) {
        
        List<FinancialRecordDTO> records = recordService.getRecentTransactions(currentUser.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordDTO>> createRecord(
            @Valid @RequestBody FinancialRecordRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Creating financial record by user: {}", currentUser.getUsername());
        FinancialRecordDTO record = recordService.createRecord(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Record created successfully", record));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordDTO>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Updating financial record: {} by user: {}", id, currentUser.getUsername());
        FinancialRecordDTO record = recordService.updateRecord(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Record updated successfully", record));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Deleting financial record: {} by user: {}", id, currentUser.getUsername());
        recordService.deleteRecord(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Record deleted successfully"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordDTO>> cancelRecord(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Cancelling financial record: {} by user: {}", id, currentUser.getUsername());
        FinancialRecordDTO record = recordService.cancelRecord(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Record cancelled successfully", record));
    }
}
