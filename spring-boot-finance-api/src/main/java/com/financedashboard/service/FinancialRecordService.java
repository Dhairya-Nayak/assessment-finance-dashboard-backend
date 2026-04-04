package com.financedashboard.service;

import com.financedashboard.dto.request.FinancialRecordRequest;
import com.financedashboard.dto.request.RecordFilterRequest;
import com.financedashboard.dto.response.FinancialRecordDTO;
import com.financedashboard.dto.response.PagedResponse;
import com.financedashboard.entity.Category;
import com.financedashboard.entity.FinancialRecord;
import com.financedashboard.entity.User;
import com.financedashboard.exception.BadRequestException;
import com.financedashboard.exception.ResourceNotFoundException;
import com.financedashboard.repository.CategoryRepository;
import com.financedashboard.repository.FinancialRecordRepository;
import com.financedashboard.repository.UserRepository;
import com.financedashboard.security.UserPrincipal;
import com.financedashboard.specification.FinancialRecordSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public FinancialRecordDTO getRecordById(Long id, UserPrincipal currentUser) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial Record", "id", id));

        // Check ownership for non-admins
        if (!currentUser.isAdmin() && !record.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Financial Record", "id", id);
        }

        return FinancialRecordDTO.fromEntity(record);
    }

    @Transactional(readOnly = true)
    public PagedResponse<FinancialRecordDTO> getRecords(RecordFilterRequest filter, UserPrincipal currentUser) {
        Sort sort = filter.getSortDirection().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Non-admins can only see their own records
        Long userId = currentUser.isAdmin() ? null : currentUser.getId();

        Specification<FinancialRecord> spec = FinancialRecordSpecification.buildSpecification(filter, userId);
        Page<FinancialRecord> recordsPage = recordRepository.findAll(spec, pageable);

        List<FinancialRecordDTO> recordDTOs = recordsPage.getContent().stream()
                .map(FinancialRecordDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.from(recordsPage, recordDTOs);
    }

    @Transactional(readOnly = true)
    public PagedResponse<FinancialRecordDTO> getUserRecords(Long userId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FinancialRecord> recordsPage = recordRepository.findAllByUserIdAndStatusNot(
                userId, FinancialRecord.RecordStatus.DELETED, pageable);

        List<FinancialRecordDTO> recordDTOs = recordsPage.getContent().stream()
                .map(FinancialRecordDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.from(recordsPage, recordDTOs);
    }

    @Transactional(readOnly = true)
    public List<FinancialRecordDTO> getRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return recordRepository.findByUserIdAndDateRange(userId, startDate, endDate).stream()
                .map(FinancialRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<FinancialRecordDTO> searchRecords(Long userId, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<FinancialRecord> recordsPage = recordRepository.searchRecords(userId, search, pageable);

        List<FinancialRecordDTO> recordDTOs = recordsPage.getContent().stream()
                .map(FinancialRecordDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.from(recordsPage, recordDTOs);
    }

    @Transactional
    @CacheEvict(value = {"dashboardSummary", "categorySummary", "monthlyTrends"}, allEntries = true)
    public FinancialRecordDTO createRecord(FinancialRecordRequest request, UserPrincipal currentUser) {
        // Get category and validate
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        // Validate category type matches record type
        if (category.getType() != Category.CategoryType.BOTH &&
            !category.getType().name().equals(request.getType().name())) {
            throw new BadRequestException("Category type does not match record type");
        }

        // Get user
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Validate recurring frequency if recurring
        if (Boolean.TRUE.equals(request.getIsRecurring()) && request.getRecurringFrequency() == null) {
            throw new BadRequestException("Recurring frequency is required for recurring records");
        }

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(category)
                .user(user)
                .description(request.getDescription())
                .referenceNumber(request.getReferenceNumber())
                .transactionDate(request.getTransactionDate())
                .notes(request.getNotes())
                .tags(request.getTags())
                .status(FinancialRecord.RecordStatus.CONFIRMED)
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .recurringFrequency(request.getRecurringFrequency())
                .attachmentUrl(request.getAttachmentUrl())
                .createdBy(currentUser.getId())
                .build();

        record = recordRepository.save(record);
        log.info("Financial record created: {} by user {}", record.getId(), currentUser.getUsername());

        return FinancialRecordDTO.fromEntity(record);
    }

    @Transactional
    @CacheEvict(value = {"dashboardSummary", "categorySummary", "monthlyTrends"}, allEntries = true)
    public FinancialRecordDTO updateRecord(Long id, FinancialRecordRequest request, UserPrincipal currentUser) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial Record", "id", id));

        // Check ownership for non-admins
        if (!currentUser.isAdmin() && !record.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Financial Record", "id", id);
        }

        // Get category if changed
        if (!record.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

            // Validate category type
            if (category.getType() != Category.CategoryType.BOTH &&
                !category.getType().name().equals(request.getType().name())) {
                throw new BadRequestException("Category type does not match record type");
            }

            record.setCategory(category);
        }

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setDescription(request.getDescription());
        record.setReferenceNumber(request.getReferenceNumber());
        record.setTransactionDate(request.getTransactionDate());
        record.setNotes(request.getNotes());
        record.setTags(request.getTags());
        record.setIsRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false);
        record.setRecurringFrequency(request.getRecurringFrequency());
        record.setAttachmentUrl(request.getAttachmentUrl());
        record.setLastModifiedBy(currentUser.getId());

        record = recordRepository.save(record);
        log.info("Financial record updated: {} by user {}", record.getId(), currentUser.getUsername());

        return FinancialRecordDTO.fromEntity(record);
    }

    @Transactional
    @CacheEvict(value = {"dashboardSummary", "categorySummary", "monthlyTrends"}, allEntries = true)
    public void deleteRecord(Long id, UserPrincipal currentUser) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial Record", "id", id));

        // Check ownership for non-admins
        if (!currentUser.isAdmin() && !record.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Financial Record", "id", id);
        }

        // Soft delete
        record.setStatus(FinancialRecord.RecordStatus.DELETED);
        record.setLastModifiedBy(currentUser.getId());
        recordRepository.save(record);

        log.info("Financial record deleted: {} by user {}", id, currentUser.getUsername());
    }

    @Transactional
    @CacheEvict(value = {"dashboardSummary", "categorySummary", "monthlyTrends"}, allEntries = true)
    public FinancialRecordDTO cancelRecord(Long id, UserPrincipal currentUser) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial Record", "id", id));

        // Check ownership for non-admins
        if (!currentUser.isAdmin() && !record.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Financial Record", "id", id);
        }

        record.setStatus(FinancialRecord.RecordStatus.CANCELLED);
        record.setLastModifiedBy(currentUser.getId());
        record = recordRepository.save(record);

        log.info("Financial record cancelled: {} by user {}", id, currentUser.getUsername());

        return FinancialRecordDTO.fromEntity(record);
    }

    @Transactional(readOnly = true)
    public List<FinancialRecordDTO> getRecentTransactions(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return recordRepository.findRecentTransactions(userId, pageable).getContent().stream()
                .map(FinancialRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
