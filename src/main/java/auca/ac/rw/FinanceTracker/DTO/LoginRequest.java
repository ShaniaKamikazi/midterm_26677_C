package auca.ac.rw.FinanceTracker.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Identifier (email or username) is required")
    private String userIdentifier;

    @NotBlank(message = "Password is required")
    private String password;

    // Optional on first login attempt; required when otp.email.verification=true and OTP has been sent.
    private String otp;
}
