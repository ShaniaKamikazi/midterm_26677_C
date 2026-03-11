package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.UserSettingsDTO;
import auca.ac.rw.FinanceTracker.DTO.UserSettingsRequest;

import java.util.UUID;

public interface IUserSettingsService {

    UserSettingsDTO getSettings(UUID userId);

    UserSettingsDTO updateSettings(UUID userId, UserSettingsRequest request);

    UserSettingsDTO resetSettings(UUID userId);
}
