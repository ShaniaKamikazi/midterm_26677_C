package auca.ac.rw.FinanceTracker.DTO;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialProfileRequest {

    @Positive(message = "Monthly income must be positive")
    private BigDecimal monthlyIncome;

    private String preferredCurrency;

    @Size(max = 500)
    private String financialGoal;

    private String budgetingPreference;

    private String preferredBudgetFrequency;
}
