package com.financedashboard.specification;

import com.financedashboard.dto.request.RecordFilterRequest;
import com.financedashboard.entity.FinancialRecord;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class FinancialRecordSpecification {

    public static Specification<FinancialRecord> buildSpecification(RecordFilterRequest filter, Long userId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude deleted records
            predicates.add(criteriaBuilder.notEqual(root.get("status"), FinancialRecord.RecordStatus.DELETED));

            // Filter by user if specified (for non-admin users)
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }

            // Filter by date range
            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), filter.getStartDate()));
            }
            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), filter.getEndDate()));
            }

            // Filter by type
            if (filter.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filter.getType()));
            }

            // Filter by categories
            if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
                predicates.add(root.get("category").get("id").in(filter.getCategoryIds()));
            }

            // Filter by amount range
            if (filter.getMinAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }
            if (filter.getMaxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            // Filter by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Search in description, notes, reference number
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                Predicate notesPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("notes")), searchPattern);
                Predicate referencePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("referenceNumber")), searchPattern);
                predicates.add(criteriaBuilder.or(descriptionPredicate, notesPredicate, referencePredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<FinancialRecord> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<FinancialRecord> hasType(FinancialRecord.RecordType type) {
        return (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<FinancialRecord> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<FinancialRecord> isNotDeleted() {
        return (root, query, criteriaBuilder) -> 
                criteriaBuilder.notEqual(root.get("status"), FinancialRecord.RecordStatus.DELETED);
    }
}
