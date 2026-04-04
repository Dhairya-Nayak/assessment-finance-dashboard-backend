package com.financedashboard.repository;

import com.financedashboard.entity.FinancialRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    // Basic queries
    List<FinancialRecord> findAllByUserIdAndStatusNot(Long userId, FinancialRecord.RecordStatus status);

    Page<FinancialRecord> findAllByUserIdAndStatusNot(Long userId, FinancialRecord.RecordStatus status, Pageable pageable);

    List<FinancialRecord> findAllByUserIdAndTypeAndStatusNot(Long userId, FinancialRecord.RecordType type, FinancialRecord.RecordStatus status);

    // Date range queries
    @Query("SELECT fr FROM FinancialRecord fr WHERE fr.user.id = :userId " +
           "AND fr.transactionDate BETWEEN :startDate AND :endDate " +
           "AND fr.status != 'DELETED' ORDER BY fr.transactionDate DESC")
    List<FinancialRecord> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT fr FROM FinancialRecord fr WHERE fr.user.id = :userId " +
           "AND fr.transactionDate BETWEEN :startDate AND :endDate " +
           "AND fr.status != 'DELETED'")
    Page<FinancialRecord> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // Category queries
    @Query("SELECT fr FROM FinancialRecord fr WHERE fr.user.id = :userId " +
           "AND fr.category.id = :categoryId AND fr.status != 'DELETED'")
    List<FinancialRecord> findByUserIdAndCategoryId(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId);

    // Summary queries
    @Query("SELECT COALESCE(SUM(fr.amount), 0) FROM FinancialRecord fr " +
           "WHERE fr.user.id = :userId AND fr.type = :type AND fr.status = 'CONFIRMED'")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") FinancialRecord.RecordType type);

    @Query("SELECT COALESCE(SUM(fr.amount), 0) FROM FinancialRecord fr " +
           "WHERE fr.user.id = :userId AND fr.type = :type " +
           "AND fr.transactionDate BETWEEN :startDate AND :endDate AND fr.status = 'CONFIRMED'")
    BigDecimal sumAmountByUserIdAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") FinancialRecord.RecordType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(fr) FROM FinancialRecord fr " +
           "WHERE fr.user.id = :userId AND fr.status = 'CONFIRMED'")
    long countByUserId(@Param("userId") Long userId);

    // Category-wise summary
    @Query("SELECT fr.category.id, fr.category.name, fr.category.color, SUM(fr.amount), COUNT(fr) " +
           "FROM FinancialRecord fr " +
           "WHERE fr.user.id = :userId AND fr.type = :type " +
           "AND fr.transactionDate BETWEEN :startDate AND :endDate " +
           "AND fr.status = 'CONFIRMED' " +
           "GROUP BY fr.category.id, fr.category.name, fr.category.color " +
           "ORDER BY SUM(fr.amount) DESC")
    List<Object[]> getCategoryWiseSummary(
            @Param("userId") Long userId,
            @Param("type") FinancialRecord.RecordType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Monthly trends
    @Query(value = "SELECT YEAR(transaction_date) as year, MONTH(transaction_date) as month, " +
                   "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income, " +
                   "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense " +
                   "FROM financial_records " +
                   "WHERE user_id = :userId AND status = 'CONFIRMED' " +
                   "AND transaction_date >= :startDate " +
                   "GROUP BY YEAR(transaction_date), MONTH(transaction_date) " +
                   "ORDER BY year DESC, month DESC",
           nativeQuery = true)
    List<Object[]> getMonthlyTrends(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    // Weekly trends
    @Query(value = "SELECT YEARWEEK(transaction_date, 1) as week, " +
                   "MIN(transaction_date) as week_start, " +
                   "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income, " +
                   "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense " +
                   "FROM financial_records " +
                   "WHERE user_id = :userId AND status = 'CONFIRMED' " +
                   "AND transaction_date >= :startDate " +
                   "GROUP BY YEARWEEK(transaction_date, 1) " +
                   "ORDER BY week DESC",
           nativeQuery = true)
    List<Object[]> getWeeklyTrends(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    // Daily summary
    @Query(value = "SELECT transaction_date, " +
                   "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income, " +
                   "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense " +
                   "FROM financial_records " +
                   "WHERE user_id = :userId AND status = 'CONFIRMED' " +
                   "AND transaction_date BETWEEN :startDate AND :endDate " +
                   "GROUP BY transaction_date " +
                   "ORDER BY transaction_date DESC",
           nativeQuery = true)
    List<Object[]> getDailySummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Recent transactions
    @Query("SELECT fr FROM FinancialRecord fr WHERE fr.user.id = :userId " +
           "AND fr.status = 'CONFIRMED' ORDER BY fr.transactionDate DESC, fr.createdAt DESC")
    Page<FinancialRecord> findRecentTransactions(@Param("userId") Long userId, Pageable pageable);

    // Search
    @Query("SELECT fr FROM FinancialRecord fr WHERE fr.user.id = :userId " +
           "AND fr.status != 'DELETED' " +
           "AND (LOWER(fr.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(fr.notes) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(fr.referenceNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<FinancialRecord> searchRecords(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

    // For all users (admin)
    @Query("SELECT fr FROM FinancialRecord fr WHERE fr.status != 'DELETED'")
    Page<FinancialRecord> findAllActiveRecords(Pageable pageable);

    // Top categories
    @Query("SELECT fr.category.id, fr.category.name, SUM(fr.amount) as total " +
           "FROM FinancialRecord fr " +
           "WHERE fr.user.id = :userId AND fr.type = :type " +
           "AND fr.transactionDate BETWEEN :startDate AND :endDate " +
           "AND fr.status = 'CONFIRMED' " +
           "GROUP BY fr.category.id, fr.category.name " +
           "ORDER BY total DESC")
    List<Object[]> getTopCategories(
            @Param("userId") Long userId,
            @Param("type") FinancialRecord.RecordType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
