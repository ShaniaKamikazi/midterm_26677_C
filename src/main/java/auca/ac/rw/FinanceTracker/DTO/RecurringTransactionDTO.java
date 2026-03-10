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
public class RecurringTransactionDTO {
    private UUID recurringTransactionId;
    private UUID accountId;
    private String accountName;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String description;
    private String transactionType;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextExecutionDate;
    private boolean active;
    private LocalDateTime createdAt;
}
