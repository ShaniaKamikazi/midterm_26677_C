package auca.ac.rw.FinanceTracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@Entity
@Table(name = "user_settings")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserSettings {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Appearance
    @Column(name = "theme", length = 20)
    private String theme = "light"; // light, dark, system

    @Column(name = "language", length = 10)
    private String language = "en"; // en, fr, rw

    // Notifications
    @Column(name = "email_notifications", nullable = false)
    private boolean emailNotifications = true;

    @Column(name = "push_notifications", nullable = false)
    private boolean pushNotifications = true;

    @Column(name = "budget_alerts", nullable = false)
    private boolean budgetAlerts = true;

    @Column(name = "weekly_reports", nullable = false)
    private boolean weeklyReports = false;

    @Column(name = "monthly_reports", nullable = false)
    private boolean monthlyReports = true;

    // Privacy
    @Column(name = "show_balance", nullable = false)
    private boolean showBalance = true;

    @Column(name = "two_factor_enabled", nullable = false)
    private boolean twoFactorEnabled = false;

    // Dashboard Preferences
    @Column(name = "default_date_range", length = 20)
    private String defaultDateRange = "month"; // week, month, quarter, year

    @Column(name = "dashboard_widgets", length = 500)
    private String dashboardWidgets = "balance,expenses,budgets,goals"; // comma-separated

    // Convenience constructor
    public UserSettings(User user) {
        this.user = user;
        this.userId = user.getUserId();
    }
}
