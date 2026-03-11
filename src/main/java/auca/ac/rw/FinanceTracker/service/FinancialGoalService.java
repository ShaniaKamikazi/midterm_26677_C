package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.FinancialGoalDTO;
import auca.ac.rw.FinanceTracker.DTO.FinancialGoalRequest;
import auca.ac.rw.FinanceTracker.DTO.GoalContributionRequest;
import auca.ac.rw.FinanceTracker.enums.GoalStatus;
import auca.ac.rw.FinanceTracker.exception.BadRequestException;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.exception.UnauthorizedException;
import auca.ac.rw.FinanceTracker.model.Account;
import auca.ac.rw.FinanceTracker.model.FinancialGoal;
import auca.ac.rw.FinanceTracker.model.User;
import auca.ac.rw.FinanceTracker.repository.IAccountRepository;
import auca.ac.rw.FinanceTracker.repository.IFinancialGoalRepository;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FinancialGoalService implements IFinancialGoalService {

    private static final Logger log = LoggerFactory.getLogger(FinancialGoalService.class);

    private final IFinancialGoalRepository goalRepository;
    private final IUserRepository userRepository;
    private final IAccountRepository accountRepository;

    public FinancialGoalService(IFinancialGoalRepository goalRepository,
                                IUserRepository userRepository,
                                IAccountRepository accountRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public FinancialGoalDTO createGoal(UUID userId, FinancialGoalRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FinancialGoal goal = new FinancialGoal();
        goal.setUser(user);
        goal.setName(request.getName());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setStatus(GoalStatus.IN_PROGRESS);

        if (request.getLinkedAccountId() != null) {
            Account account = accountRepository.findByAccountIdAndDeletedFalse(request.getLinkedAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
            if (!account.getUser().getUserId().equals(userId)) {
                throw new UnauthorizedException("You do not have access to this account");
            }
            goal.setLinkedAccount(account);
        }

        goal = goalRepository.save(goal);
        log.info("Financial goal created: {} for user {}", request.getName(), userId);
        return toDTO(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialGoalDTO getGoalById(UUID goalId, UUID userId) {
        FinancialGoal goal = goalRepository.findByGoalIdAndDeletedFalse(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found"));
        verifyOwnership(goal, userId);
        return toDTO(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialGoalDTO> getGoalsByUser(UUID userId) {
        return goalRepository.findByUserUserIdAndDeletedFalse(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialGoalDTO> getGoalsByUserPaginated(UUID userId, Pageable pageable) {
        return goalRepository.findByUserUserIdAndDeletedFalse(userId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public FinancialGoalDTO updateGoal(UUID goalId, UUID userId, FinancialGoalRequest request) {
        FinancialGoal goal = goalRepository.findByGoalIdAndDeletedFalse(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found"));
        verifyOwnership(goal, userId);

        goal.setName(request.getName());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());

        if (request.getLinkedAccountId() != null) {
            Account account = accountRepository.findByAccountIdAndDeletedFalse(request.getLinkedAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
            if (!account.getUser().getUserId().equals(userId)) {
                throw new UnauthorizedException("You do not have access to this account");
            }
            goal.setLinkedAccount(account);
        } else {
            goal.setLinkedAccount(null);
        }

        // Check if goal is now completed
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }

        goal = goalRepository.save(goal);
        log.info("Financial goal updated: {}", goalId);
        return toDTO(goal);
    }

    @Override
    @Transactional
    public FinancialGoalDTO contributeToGoal(UUID goalId, UUID userId, GoalContributionRequest request) {
        FinancialGoal goal = goalRepository.findByGoalIdAndDeletedFalse(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found"));
        verifyOwnership(goal, userId);

        if (goal.getStatus() == GoalStatus.COMPLETED) {
            throw new BadRequestException("Goal is already completed");
        }
        if (goal.getStatus() == GoalStatus.CANCELLED) {
            throw new BadRequestException("Goal has been cancelled");
        }

        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
            log.info("Financial goal completed: {}", goalId);
        }

        goal = goalRepository.save(goal);
        log.info("Contributed {} to goal {} for user {}", request.getAmount(), goalId, userId);
        return toDTO(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(UUID goalId, UUID userId) {
        FinancialGoal goal = goalRepository.findByGoalIdAndDeletedFalse(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found"));
        verifyOwnership(goal, userId);

        goal.softDelete();
        goalRepository.save(goal);
        log.info("Financial goal soft-deleted: {}", goalId);
    }

    private void verifyOwnership(FinancialGoal goal, UUID userId) {
        if (!goal.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this financial goal");
        }
    }

    private FinancialGoalDTO toDTO(FinancialGoal goal) {
        double percentComplete = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? goal.getCurrentAmount().doubleValue() / goal.getTargetAmount().doubleValue() * 100.0
                : 0.0;

        return new FinancialGoalDTO(
                goal.getGoalId(),
                goal.getName(),
                goal.getDescription(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                Math.round(percentComplete * 100.0) / 100.0,
                goal.getTargetDate(),
                goal.getStatus().name(),
                goal.getLinkedAccount() != null ? goal.getLinkedAccount().getAccountId() : null,
                goal.getLinkedAccount() != null ? goal.getLinkedAccount().getAccountName() : null,
                goal.getCreatedAt()
        );
    }
}
