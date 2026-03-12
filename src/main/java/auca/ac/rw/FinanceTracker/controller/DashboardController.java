package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.*;
import auca.ac.rw.FinanceTracker.service.IAccountService;
import auca.ac.rw.FinanceTracker.service.IBudgetService;
import auca.ac.rw.FinanceTracker.service.ITransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IAccountService accountService;
    private final IBudgetService budgetService;
    private final ITransactionService transactionService;

    public DashboardController(IAccountService accountService,
                               IBudgetService budgetService,
                               ITransactionService transactionService) {
        this.accountService = accountService;
        this.budgetService = budgetService;
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboard(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        List<AccountDTO> accounts = accountService.getAccountsByUser(userId);
        List<BudgetDTO> budgets = budgetService.getBudgetsByUser(userId);
        List<TransactionDTO> transactions = transactionService.getTransactionsByUser(userId);

        BigDecimal totalBalance = accounts.stream()
                .map(AccountDTO::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return last 10 transactions as recent
        List<TransactionDTO> recentTransactions = transactions.stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .limit(10)
                .toList();

        DashboardDTO dashboard = new DashboardDTO(
                totalBalance,
                accounts.size(),
                budgets.size(),
                transactions.size(),
                accounts,
                budgets,
                recentTransactions
        );

        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
