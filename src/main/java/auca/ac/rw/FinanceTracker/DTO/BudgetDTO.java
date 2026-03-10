package auca.ac.rw.FinanceTracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {
    private UUID budgetId;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private String frequency;
    private boolean autoRenew;
    private LocalDate startDate;
    private LocalDate endDate;
    private double percentUsed;
    private String budgetStatus; // NORMAL, WARNING, EXCEEDED
}
