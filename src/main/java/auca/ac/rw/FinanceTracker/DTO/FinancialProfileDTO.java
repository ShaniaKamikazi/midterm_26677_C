package auca.ac.rw.FinanceTracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialProfileDTO {
    private BigDecimal monthlyIncome;
    private String preferredCurrency;
    private String financialGoal;
    private String budgetingPreference;
    private String preferredBudgetFrequency;
    private boolean profileCompleted;
}
