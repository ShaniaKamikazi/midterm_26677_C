package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.*;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);

    void requestPasswordReset(String email);

    void resetPassword(PasswordResetRequest request);

    void changePassword(UUID userId, ChangePasswordRequest request);

    FinancialProfileDTO setupFinancialProfile(UUID userId, FinancialProfileRequest request);

    FinancialProfileDTO getFinancialProfile(UUID userId);

    UserDTO getUserById(UUID userId);

    List<UserDTO> getAllUsers();

    UserDTO updateUser(UUID userId, UUID authenticatedUserId, SignupRequest request);

    void deleteUser(UUID userId, UUID authenticatedUserId);

    List<UserDTO> getUsersByProvince(String identifier);
}
