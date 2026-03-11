package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.InsightsDTO;
import auca.ac.rw.FinanceTracker.enums.TransactionType;
import auca.ac.rw.FinanceTracker.repository.ITransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightsService implements IInsightsService {

    private final ITransactionRepository transactionRepository;

    public InsightsService(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public InsightsDTO getMonthlyInsights(UUID userId) {
        LocalDate now = LocalDate.now();

        // Current month boundaries
        LocalDateTime currentMonthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime currentMonthEnd = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        // Previous month boundaries
        LocalDateTime previousMonthStart = now.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime previousMonthEnd = currentMonthStart;

        // Income & expense totals
        BigDecimal currentIncome = transactionRepository.sumByUserAndTypeAndDateBetween(
                userId, TransactionType.INCOME, currentMonthStart, currentMonthEnd);
        BigDecimal currentExpenses = transactionRepository.sumByUserAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, currentMonthStart, currentMonthEnd);
        BigDecimal previousIncome = transactionRepository.sumByUserAndTypeAndDateBetween(
                userId, TransactionType.INCOME, previousMonthStart, previousMonthEnd);
        BigDecimal previousExpenses = transactionRepository.sumByUserAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, previousMonthStart, previousMonthEnd);

        BigDecimal currentSavings = currentIncome.subtract(currentExpenses);
        BigDecimal previousSavings = previousIncome.subtract(previousExpenses);

        double incomeChange = percentChange(previousIncome, currentIncome);
        double expenseChange = percentChange(previousExpenses, currentExpenses);
        double savingsChange = percentChange(previousSavings, currentSavings);

        // Category-wise spending comparison
        List<Object[]> currentCategorySpending = transactionRepository.sumByCategoryAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, currentMonthStart, currentMonthEnd);
        List<Object[]> previousCategorySpending = transactionRepository.sumByCategoryAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, previousMonthStart, previousMonthEnd);

        Map<String, BigDecimal> previousMap = new HashMap<>();
        for (Object[] row : previousCategorySpending) {
            previousMap.put((String) row[0], (BigDecimal) row[1]);
        }

        List<InsightsDTO.CategorySpendingDTO> topCategories = currentCategorySpending.stream()
                .limit(10)
                .map(row -> {
                    String categoryName = (String) row[0];
                    BigDecimal currentAmount = (BigDecimal) row[1];
                    BigDecimal previousAmount = previousMap.getOrDefault(categoryName, BigDecimal.ZERO);
                    double change = percentChange(previousAmount, currentAmount);
                    return new InsightsDTO.CategorySpendingDTO(categoryName, currentAmount, previousAmount, change);
                })
                .collect(Collectors.toList());

        // Generate natural language insights
        List<String> insights = generateInsights(
                currentIncome, currentExpenses, previousIncome, previousExpenses,
                currentSavings, previousSavings, topCategories);

        return new InsightsDTO(
                currentIncome, currentExpenses,
                previousIncome, previousExpenses,
                currentSavings, previousSavings,
                incomeChange, expenseChange, savingsChange,
                topCategories, insights
        );
    }

    private double percentChange(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous.abs(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private List<String> generateInsights(
            BigDecimal currentIncome, BigDecimal currentExpenses,
            BigDecimal previousIncome, BigDecimal previousExpenses,
            BigDecimal currentSavings, BigDecimal previousSavings,
            List<InsightsDTO.CategorySpendingDTO> topCategories) {

        List<String> insights = new ArrayList<>();

        // Income insights
        double incomeChange = percentChange(previousIncome, currentIncome);
        if (incomeChange > 0) {
            insights.add(String.format("Your income increased by %.0f%% compared to last month", incomeChange));
        } else if (incomeChange < 0) {
            insights.add(String.format("Your income decreased by %.0f%% compared to last month", Math.abs(incomeChange)));
        }

        // Expense insights
        double expenseChange = percentChange(previousExpenses, currentExpenses);
        if (expenseChange > 10) {
            insights.add(String.format("Your spending increased by %.0f%% this month — consider reviewing your expenses", expenseChange));
        } else if (expenseChange < -10) {
            insights.add(String.format("Great job! You reduced spending by %.0f%% compared to last month", Math.abs(expenseChange)));
        }

        // Savings insights
        if (currentSavings.compareTo(previousSavings) > 0 && previousSavings.compareTo(BigDecimal.ZERO) > 0) {
            double savingsImprovement = percentChange(previousSavings, currentSavings);
            insights.add(String.format("You saved %.0f%% more than last month", savingsImprovement));
        } else if (currentSavings.compareTo(BigDecimal.ZERO) < 0) {
            insights.add("Warning: You're spending more than you earn this month");
        }

        // Category insights
        for (InsightsDTO.CategorySpendingDTO cat : topCategories) {
            if (cat.getChangePercent() > 20 && cat.getPreviousMonthAmount().compareTo(BigDecimal.ZERO) > 0) {
                insights.add(String.format("You spend %.0f%% more on %s this month",
                        cat.getChangePercent(), cat.getCategoryName()));
            }
        }

        if (insights.isEmpty()) {
            insights.add("Your spending patterns are consistent with last month");
        }

        return insights;
    }
}
