package auca.ac.rw.FinanceTracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private UUID transactionId;
    private UUID accountId;
    private BigDecimal amount;
    private LocalDateTime date;
    private String description;
    private String transactionType;
    private UUID categoryId;
    private String categoryName;
    private List<TagDTO> tags;
    private LocalDateTime createdAt;
}
