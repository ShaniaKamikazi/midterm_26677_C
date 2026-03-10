package auca.ac.rw.FinanceTracker.DTO;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserSettingsRequest {
    @Size(max = 20)
    private String theme;

    @Size(max = 10)
    private String language;

    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean budgetAlerts;
    private Boolean weeklyReports;
    private Boolean monthlyReports;
    private Boolean showBalance;
    private Boolean twoFactorEnabled;

    @Size(max = 20)
    private String defaultDateRange;

    @Size(max = 500)
    private String dashboardWidgets;
}
