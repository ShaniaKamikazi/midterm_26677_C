package auca.ac.rw.FinanceTracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import auca.ac.rw.FinanceTracker.model.Transaction;
import auca.ac.rw.FinanceTracker.enums.TransactionType;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByAccountAccountIdAndDeletedFalse(UUID accountId);

    List<Transaction> findByUserUserIdAndDeletedFalse(UUID userId);

    List<Transaction> findByCategoryCategoryIdAndDeletedFalse(UUID categoryId);

    Optional<Transaction> findByTransactionIdAndDeletedFalse(UUID id);

    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId AND t.deleted = false " +
           "AND (:type IS NULL OR t.transactionType = :type) " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "ORDER BY t.date DESC")
    Page<Transaction> findByUserFiltered(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.userId = :userId AND t.category.categoryId = :categoryId AND t.transactionType = :type AND t.date BETWEEN :start AND :end AND t.deleted = false")
    BigDecimal sumByUserAndCategoryAndTypeAndDateBetween(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("type") TransactionType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.userId = :userId AND t.transactionType = :type AND t.date BETWEEN :start AND :end AND t.deleted = false")
    BigDecimal sumByUserAndTypeAndDateBetween(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId AND t.deleted = false AND (" +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Transaction> searchByDescription(@Param("userId") UUID userId, @Param("searchTerm") String searchTerm);

    Optional<Transaction> findByTransactionIdAndDeletedTrue(UUID transactionId);

    @Query("SELECT t.category.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.userId = :userId AND t.transactionType = :type " +
           "AND t.date BETWEEN :start AND :end AND t.deleted = false AND t.category IS NOT NULL " +
           "GROUP BY t.category.name ORDER BY SUM(t.amount) DESC")
    List<Object[]> sumByCategoryAndTypeAndDateBetween(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}


