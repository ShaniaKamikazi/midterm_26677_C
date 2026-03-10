package auca.ac.rw.FinanceTracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialGoalDTO {
    private UUID goalId;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private double percentComplete;
    private LocalDate targetDate;
    private String status;
    private UUID linkedAccountId;
    private String linkedAccountName;
    private LocalDateTime createdAt;
}
