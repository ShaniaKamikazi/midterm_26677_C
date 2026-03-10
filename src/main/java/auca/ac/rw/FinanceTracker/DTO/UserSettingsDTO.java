package auca.ac.rw.FinanceTracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserSettingsDTO {
    private String theme;
    private String language;
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean budgetAlerts;
    private boolean weeklyReports;
    private boolean monthlyReports;
    private boolean showBalance;
    private boolean twoFactorEnabled;
    private String defaultDateRange;
    private String dashboardWidgets;
}
