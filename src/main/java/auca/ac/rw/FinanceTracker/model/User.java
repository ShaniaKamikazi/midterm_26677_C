package auca.ac.rw.FinanceTracker.model;

import auca.ac.rw.FinanceTracker.enums.AccountType;
import auca.ac.rw.FinanceTracker.enums.BudgetingPreference;
import auca.ac.rw.FinanceTracker.enums.Currency;
import auca.ac.rw.FinanceTracker.enums.RecurrenceFrequency;
import auca.ac.rw.FinanceTracker.enums.RoleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "user_name", unique = true, nullable = false)
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "user_email", unique = true, nullable = false)
    private String userEmail;

    @JsonIgnore
    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false)
    private RoleType role;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private java.time.LocalDateTime passwordResetTokenExpiry;

    @Column(name = "login_otp", length = 10)
    private String loginOtp;

    @Column(name = "login_otp_expiry")
    private java.time.LocalDateTime loginOtpExpiry;

    // Financial Profile fields
    @Column(name = "monthly_income", precision = 19, scale = 4)
    private BigDecimal monthlyIncome;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_currency")
    private Currency preferredCurrency = Currency.RWF;

    @Size(max = 500)
    @Column(name = "financial_goal", length = 500)
    private String financialGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "budgeting_preference")
    private BudgetingPreference budgetingPreference = BudgetingPreference.MODERATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_budget_frequency")
    private RecurrenceFrequency preferredBudgetFrequency = RecurrenceFrequency.MONTHLY;

    @Column(name = "profile_completed", nullable = false, columnDefinition = "boolean not null default false")
    private boolean profileCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id")
    private Location village;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> categories;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Budget> budgets;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reports;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserSettings settings;
}