package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.BudgetDTO;
import auca.ac.rw.FinanceTracker.DTO.BudgetRequest;
import auca.ac.rw.FinanceTracker.exception.BadRequestException;
import auca.ac.rw.FinanceTracker.exception.DuplicateResourceException;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.exception.UnauthorizedException;
import auca.ac.rw.FinanceTracker.enums.RecurrenceFrequency;
import auca.ac.rw.FinanceTracker.enums.TransactionType;
import auca.ac.rw.FinanceTracker.model.*;
import auca.ac.rw.FinanceTracker.repository.IBudgetRepository;
import auca.ac.rw.FinanceTracker.repository.ICategoryRepository;
import auca.ac.rw.FinanceTracker.repository.ITransactionRepository;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BudgetService implements IBudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final IBudgetRepository budgetRepository;
    private final IUserRepository userRepository;
    private final ICategoryRepository categoryRepository;
    private final ITransactionRepository transactionRepository;

    public BudgetService(IBudgetRepository budgetRepository,
                         IUserRepository userRepository,
                         ICategoryRepository categoryRepository,
                         ITransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public BudgetDTO createBudget(UUID userId, BudgetRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        RecurrenceFrequency frequency = request.getFrequency();

        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fall back to user's preferred budget frequency if none specified
        if (frequency == null && user.getPreferredBudgetFrequency() != null) {
            frequency = user.getPreferredBudgetFrequency();
        }

        if (frequency != null) {
            if (startDate == null) {
                startDate = LocalDate.now();
            }
            endDate = calculateEndDate(startDate, frequency);
        } else {
            if (startDate == null || endDate == null) {
                throw new BadRequestException("Start date and end date are required when frequency is not provided");
            }
            if (endDate.isBefore(startDate)) {
                throw new BadRequestException("End date must be after start date");
            }
        }

        Category category = categoryRepository.findByCategoryIdAndDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this category");
        }

        // Check for duplicate budget for same user + category + period
        budgetRepository.findByUserAndCategoryAndPeriod(userId, request.getCategoryId(), startDate, endDate)
                .ifPresent(b -> {
                    throw new DuplicateResourceException("Budget already exists for this category and period");
                });

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setLimitAmount(request.getLimitAmount());
        budget.setFrequency(frequency);
        budget.setAutoRenew(request.isAutoRenew());
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);

        budget = budgetRepository.save(budget);
        log.info("Budget created for user {} category {} frequency {}", userId, request.getCategoryId(), frequency);
        return toDTO(budget, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetDTO getBudgetById(UUID budgetId, UUID userId) {
        Budget budget = budgetRepository.findByBudgetIdAndDeletedFalse(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        verifyOwnership(budget, userId);
        return toDTO(budget, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetDTO> getBudgetsByUser(UUID userId) {
        return budgetRepository.findByUserUserIdAndDeletedFalse(userId).stream()
                .map(b -> toDTO(b, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BudgetDTO> getBudgetsByUserPaginated(UUID userId, Pageable pageable) {
        return budgetRepository.findByUserUserIdAndDeletedFalse(userId, pageable)
                .map(b -> toDTO(b, userId));
    }

    @Override
    @Transactional
    public void deleteBudget(UUID budgetId, UUID userId) {
        Budget budget = budgetRepository.findByBudgetIdAndDeletedFalse(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        verifyOwnership(budget, userId);

        budget.softDelete();
        budgetRepository.save(budget);
        log.info("Budget soft-deleted: {}", budgetId);
    }

    @Override
    @Transactional
    public BudgetDTO restoreBudget(UUID budgetId, UUID userId) {
        Budget budget = budgetRepository.findByBudgetIdAndDeletedTrue(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted budget not found"));
        verifyOwnership(budget, userId);

        budget.restore();
        budget = budgetRepository.save(budget);
        log.info("Budget restored: {}", budgetId);
        return toDTO(budget, userId);
    }

    @Override
    @Transactional
    public BudgetDTO updateBudget(UUID budgetId, UUID userId, BudgetRequest request) {
        Budget budget = budgetRepository.findByBudgetIdAndDeletedFalse(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        verifyOwnership(budget, userId);

        Category category = categoryRepository.findByCategoryIdAndDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this category");
        }

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        RecurrenceFrequency frequency = request.getFrequency();

        if (frequency != null) {
            if (startDate == null) {
                startDate = budget.getStartDate();
            }
            endDate = calculateEndDate(startDate, frequency);
        } else {
            if (startDate == null) startDate = budget.getStartDate();
            if (endDate == null) endDate = budget.getEndDate();
            if (endDate.isBefore(startDate)) {
                throw new BadRequestException("End date must be after start date");
            }
        }

        budget.setCategory(category);
        budget.setLimitAmount(request.getLimitAmount());
        budget.setFrequency(frequency);
        budget.setAutoRenew(request.isAutoRenew());
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);

        budget = budgetRepository.save(budget);
        log.info("Budget updated: {}", budgetId);
        return toDTO(budget, userId);
    }

    private void verifyOwnership(Budget budget, UUID userId) {
        if (!budget.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this budget");
        }
    }

    private BudgetDTO toDTO(Budget budget, UUID userId) {
        LocalDate start = budget.getStartDate();
        LocalDate end = budget.getEndDate();

        BigDecimal spent = transactionRepository.sumByUserAndCategoryAndTypeAndDateBetween(
                userId,
                budget.getCategory().getCategoryId(),
                TransactionType.EXPENSE,
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay());

        BigDecimal remaining = budget.getLimitAmount().subtract(spent);

        double percentUsed = budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0
                ? spent.doubleValue() / budget.getLimitAmount().doubleValue() * 100.0
                : 0.0;

        String budgetStatus;
        if (percentUsed >= 100.0) {
            budgetStatus = "EXCEEDED";
        } else if (percentUsed >= 80.0) {
            budgetStatus = "WARNING";
        } else {
            budgetStatus = "NORMAL";
        }

        return new BudgetDTO(
                budget.getBudgetId(),
                budget.getCategory().getCategoryId(),
                budget.getCategory().getName(),
                budget.getLimitAmount(),
                spent,
                remaining,
                budget.getFrequency() != null ? budget.getFrequency().name() : null,
                budget.isAutoRenew(),
                budget.getStartDate(),
                budget.getEndDate(),
                Math.round(percentUsed * 100.0) / 100.0,
                budgetStatus
        );
    }

    static LocalDate calculateEndDate(LocalDate startDate, RecurrenceFrequency frequency) {
        return switch (frequency) {
            case DAILY -> startDate.plusDays(1);
            case WEEKLY -> startDate.plusWeeks(1);
            case MONTHLY -> startDate.plusMonths(1);
            case YEARLY -> startDate.plusYears(1);
        };
    }

    @Scheduled(cron = "0 0 0 * * *") // runs daily at midnight
    @Transactional
    public void renewExpiredBudgets() {
        LocalDate today = LocalDate.now();
        List<Budget> expired = budgetRepository.findExpiredAutoRenewBudgets(today);
        for (Budget old : expired) {
            LocalDate newStart = old.getEndDate();
            LocalDate newEnd = calculateEndDate(newStart, old.getFrequency());

            // Only create if no duplicate exists
            if (budgetRepository.findByUserAndCategoryAndPeriod(
                    old.getUser().getUserId(), old.getCategory().getCategoryId(), newStart, newEnd).isPresent()) {
                continue;
            }

            Budget renewed = new Budget();
            renewed.setUser(old.getUser());
            renewed.setCategory(old.getCategory());
            renewed.setLimitAmount(old.getLimitAmount());
            renewed.setFrequency(old.getFrequency());
            renewed.setAutoRenew(true);
            renewed.setStartDate(newStart);
            renewed.setEndDate(newEnd);
            budgetRepository.save(renewed);
            log.info("Auto-renewed budget {} → new period {}-{}", old.getBudgetId(), newStart, newEnd);
        }
    }
}
