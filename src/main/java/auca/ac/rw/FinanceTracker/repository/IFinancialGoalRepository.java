package auca.ac.rw.FinanceTracker.repository;

import auca.ac.rw.FinanceTracker.model.FinancialGoal;
import auca.ac.rw.FinanceTracker.enums.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IFinancialGoalRepository extends JpaRepository<FinancialGoal, UUID> {

    List<FinancialGoal> findByUserUserIdAndDeletedFalse(UUID userId);

    Page<FinancialGoal> findByUserUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    List<FinancialGoal> findByUserUserIdAndStatusAndDeletedFalse(UUID userId, GoalStatus status);

    Optional<FinancialGoal> findByGoalIdAndDeletedFalse(UUID goalId);
}
