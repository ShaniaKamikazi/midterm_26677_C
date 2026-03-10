package auca.ac.rw.FinanceTracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private UUID accountId;
    private String accountName;
    private String financialAccountType;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}
