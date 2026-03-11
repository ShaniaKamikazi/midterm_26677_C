package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.UserSettingsDTO;
import auca.ac.rw.FinanceTracker.DTO.UserSettingsRequest;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.model.User;
import auca.ac.rw.FinanceTracker.model.UserSettings;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import auca.ac.rw.FinanceTracker.repository.IUserSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserSettingsService implements IUserSettingsService {

    private static final Logger log = LoggerFactory.getLogger(UserSettingsService.class);

    private final IUserSettingsRepository settingsRepository;
    private final IUserRepository userRepository;

    public UserSettingsService(IUserSettingsRepository settingsRepository, IUserRepository userRepository) {
        this.settingsRepository = settingsRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSettingsDTO getSettings(UUID userId) {
        UserSettings settings = settingsRepository.findByUserUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        return toDTO(settings);
    }

    @Override
    @Transactional
    public UserSettingsDTO updateSettings(UUID userId, UserSettingsRequest request) {
        UserSettings settings = settingsRepository.findByUserUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        // Update only non-null fields
        if (request.getTheme() != null) settings.setTheme(request.getTheme());
        if (request.getLanguage() != null) settings.setLanguage(request.getLanguage());
        if (request.getEmailNotifications() != null) settings.setEmailNotifications(request.getEmailNotifications());
        if (request.getPushNotifications() != null) settings.setPushNotifications(request.getPushNotifications());
        if (request.getBudgetAlerts() != null) settings.setBudgetAlerts(request.getBudgetAlerts());
        if (request.getWeeklyReports() != null) settings.setWeeklyReports(request.getWeeklyReports());
        if (request.getMonthlyReports() != null) settings.setMonthlyReports(request.getMonthlyReports());
        if (request.getShowBalance() != null) settings.setShowBalance(request.getShowBalance());
        if (request.getTwoFactorEnabled() != null) settings.setTwoFactorEnabled(request.getTwoFactorEnabled());
        if (request.getDefaultDateRange() != null) settings.setDefaultDateRange(request.getDefaultDateRange());
        if (request.getDashboardWidgets() != null) settings.setDashboardWidgets(request.getDashboardWidgets());

        settings = settingsRepository.save(settings);
        log.info("Settings updated for user: {}", userId);
        return toDTO(settings);
    }

    @Override
    @Transactional
    public UserSettingsDTO resetSettings(UUID userId) {
        UserSettings settings = settingsRepository.findByUserUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        // Reset to defaults
        settings.setTheme("light");
        settings.setLanguage("en");
        settings.setEmailNotifications(true);
        settings.setPushNotifications(true);
        settings.setBudgetAlerts(true);
        settings.setWeeklyReports(false);
        settings.setMonthlyReports(true);
        settings.setShowBalance(true);
        settings.setTwoFactorEnabled(false);
        settings.setDefaultDateRange("month");
        settings.setDashboardWidgets("balance,expenses,budgets,goals");

        settings = settingsRepository.save(settings);
        log.info("Settings reset for user: {}", userId);
        return toDTO(settings);
    }

    private UserSettings createDefaultSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserSettings settings = new UserSettings(user);
        return settingsRepository.save(settings);
    }

    private UserSettingsDTO toDTO(UserSettings settings) {
        return new UserSettingsDTO(
                settings.getTheme(),
                settings.getLanguage(),
                settings.isEmailNotifications(),
                settings.isPushNotifications(),
                settings.isBudgetAlerts(),
                settings.isWeeklyReports(),
                settings.isMonthlyReports(),
                settings.isShowBalance(),
                settings.isTwoFactorEnabled(),
                settings.getDefaultDateRange(),
                settings.getDashboardWidgets()
        );
    }
}
