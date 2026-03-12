package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.FinancialProfileDTO;
import auca.ac.rw.FinanceTracker.DTO.FinancialProfileRequest;
import auca.ac.rw.FinanceTracker.DTO.SignupRequest;
import auca.ac.rw.FinanceTracker.DTO.UserDTO;
import auca.ac.rw.FinanceTracker.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me/financial-profile")
    public ResponseEntity<ApiResponse<FinancialProfileDTO>> setupFinancialProfile(
            @Valid @RequestBody FinancialProfileRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        FinancialProfileDTO profile = userService.setupFinancialProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Financial profile updated successfully", profile));
    }

    @GetMapping("/me/financial-profile")
    public ResponseEntity<ApiResponse<FinancialProfileDTO>> getFinancialProfile(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        FinancialProfileDTO profile = userService.getFinancialProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable UUID id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/by-province")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByProvince(
            @RequestParam String province) {
        List<UserDTO> users = userService.getUsersByProvince(province);
        return ResponseEntity.ok(ApiResponse.success("Users in province '" + province + "' retrieved", users));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody SignupRequest request,
            Authentication authentication) {
        UUID authenticatedUserId = (UUID) authentication.getPrincipal();
        UserDTO updatedUser = userService.updateUser(id, authenticatedUserId, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID authenticatedUserId = (UUID) authentication.getPrincipal();
        userService.deleteUser(id, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}

