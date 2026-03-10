package auca.ac.rw.FinanceTracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsightsDTO {
    private BigDecimal currentMonthIncome;
    private BigDecimal currentMonthExpenses;
    private BigDecimal previousMonthIncome;
    private BigDecimal previousMonthExpenses;
    private BigDecimal currentMonthSavings;
    private BigDecimal previousMonthSavings;
    private double incomeChangePercent;
    private double expenseChangePercent;
    private double savingsChangePercent;
    private List<CategorySpendingDTO> topSpendingCategories;
    private List<String> insights;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySpendingDTO {
        private String categoryName;
        private BigDecimal currentMonthAmount;
        private BigDecimal previousMonthAmount;
        private double changePercent;
    }
}
