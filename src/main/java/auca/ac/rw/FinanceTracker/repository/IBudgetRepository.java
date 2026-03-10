package auca.ac.rw.FinanceTracker.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import auca.ac.rw.FinanceTracker.model.Budget;

@Repository
public interface IBudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserUserIdAndDeletedFalse(UUID userId);

    Page<Budget> findByUserUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    Optional<Budget> findByBudgetIdAndDeletedFalse(UUID budgetId);

    @Query("SELECT b FROM Budget b WHERE b.user.userId = :userId AND b.category.categoryId = :categoryId AND b.startDate = :startDate AND b.endDate = :endDate AND b.deleted = false")
    Optional<Budget> findByUserAndCategoryAndPeriod(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Optional<Budget> findByBudgetIdAndDeletedTrue(UUID budgetId);

    @Query("SELECT b FROM Budget b WHERE b.autoRenew = true AND b.frequency IS NOT NULL AND b.endDate <= :today AND b.deleted = false")
    List<Budget> findExpiredAutoRenewBudgets(@Param("today") LocalDate today);
}
