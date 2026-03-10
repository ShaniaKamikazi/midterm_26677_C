package auca.ac.rw.FinanceTracker.repository;

import auca.ac.rw.FinanceTracker.model.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IRecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {

    List<RecurringTransaction> findByUserUserIdAndDeletedFalse(UUID userId);

    Optional<RecurringTransaction> findByRecurringTransactionIdAndDeletedFalse(UUID id);

    List<RecurringTransaction> findByActiveTrueAndDeletedFalseAndNextExecutionDateLessThanEqual(LocalDate date);
}
