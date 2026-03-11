package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.FinancialGoalDTO;
import auca.ac.rw.FinanceTracker.DTO.FinancialGoalRequest;
import auca.ac.rw.FinanceTracker.DTO.GoalContributionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IFinancialGoalService {

    FinancialGoalDTO createGoal(UUID userId, FinancialGoalRequest request);

    FinancialGoalDTO getGoalById(UUID goalId, UUID userId);

    List<FinancialGoalDTO> getGoalsByUser(UUID userId);

    Page<FinancialGoalDTO> getGoalsByUserPaginated(UUID userId, Pageable pageable);

    FinancialGoalDTO updateGoal(UUID goalId, UUID userId, FinancialGoalRequest request);

    FinancialGoalDTO contributeToGoal(UUID goalId, UUID userId, GoalContributionRequest request);

    void deleteGoal(UUID goalId, UUID userId);
}
