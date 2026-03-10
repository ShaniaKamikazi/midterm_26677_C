package auca.ac.rw.FinanceTracker.DTO;

import java.math.BigDecimal;
import java.util.UUID;

import auca.ac.rw.FinanceTracker.enums.AccountType;
import auca.ac.rw.FinanceTracker.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String userName;
    private String userEmail;
    private AccountType accountType;
    private RoleType role;
    private boolean enabled;
    private BigDecimal monthlyIncome;
    private String preferredCurrency;
    private String financialGoal;
    private String budgetingPreference;
    private String preferredBudgetFrequency;
    private boolean profileCompleted;
    private String villageId;
    private String villageName;
    private String locationPath;
}

