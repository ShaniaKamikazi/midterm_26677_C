package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.UserSettingsDTO;
import auca.ac.rw.FinanceTracker.DTO.UserSettingsRequest;
import auca.ac.rw.FinanceTracker.service.IUserSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    private final IUserSettingsService settingsService;

    public UserSettingsController(IUserSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserSettingsDTO>> getSettings(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserSettingsDTO settings = settingsService.getSettings(userId);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserSettingsDTO>> updateSettings(
            @Valid @RequestBody UserSettingsRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserSettingsDTO settings = settingsService.updateSettings(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Settings updated successfully", settings));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<UserSettingsDTO>> resetSettings(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserSettingsDTO settings = settingsService.resetSettings(userId);
        return ResponseEntity.ok(ApiResponse.success("Settings reset to defaults", settings));
    }
}
