package auca.ac.rw.FinanceTracker.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 100)
    private String accountName;

    private String financialAccountType;

    @NotNull(message = "Initial balance is required")
    private BigDecimal balance;
}
