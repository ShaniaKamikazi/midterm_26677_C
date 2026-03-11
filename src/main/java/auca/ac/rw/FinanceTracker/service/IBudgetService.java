package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.BudgetDTO;
import auca.ac.rw.FinanceTracker.DTO.BudgetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IBudgetService {

    BudgetDTO createBudget(UUID userId, BudgetRequest request);

    BudgetDTO getBudgetById(UUID budgetId, UUID userId);

    List<BudgetDTO> getBudgetsByUser(UUID userId);

    Page<BudgetDTO> getBudgetsByUserPaginated(UUID userId, Pageable pageable);

    BudgetDTO updateBudget(UUID budgetId, UUID userId, BudgetRequest request);

    void deleteBudget(UUID budgetId, UUID userId);

    BudgetDTO restoreBudget(UUID budgetId, UUID userId);
}
