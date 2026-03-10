package auca.ac.rw.FinanceTracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private BigDecimal totalBalance;
    private int totalAccounts;
    private int totalBudgets;
    private int totalTransactions;
    private List<AccountDTO> accounts;
    private List<BudgetDTO> activeBudgets;
    private List<TransactionDTO> recentTransactions;
}
