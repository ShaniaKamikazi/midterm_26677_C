package auca.ac.rw.FinanceTracker.DTO;

import auca.ac.rw.FinanceTracker.enums.RecurrenceFrequency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRequest {

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Limit amount is required")
    @Positive(message = "Limit amount must be positive")
    private BigDecimal limitAmount;

    private RecurrenceFrequency frequency;

    private boolean autoRenew = false;

    private LocalDate startDate;

    private LocalDate endDate;
}
