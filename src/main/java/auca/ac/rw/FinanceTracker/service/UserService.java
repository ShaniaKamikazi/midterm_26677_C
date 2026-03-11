package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.*;
import auca.ac.rw.FinanceTracker.exception.BadRequestException;
import auca.ac.rw.FinanceTracker.exception.DuplicateResourceException;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.exception.UnauthorizedException;
import auca.ac.rw.FinanceTracker.enums.AccountType;
import auca.ac.rw.FinanceTracker.enums.BudgetingPreference;
import auca.ac.rw.FinanceTracker.enums.Currency;
import auca.ac.rw.FinanceTracker.enums.RecurrenceFrequency;
import auca.ac.rw.FinanceTracker.enums.RoleType;
import auca.ac.rw.FinanceTracker.model.User;
import auca.ac.rw.FinanceTracker.model.Location;
import auca.ac.rw.FinanceTracker.repository.EmailService;
import auca.ac.rw.FinanceTracker.repository.ILocationRepository;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import auca.ac.rw.FinanceTracker.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final IUserRepository userRepository;
    private final ILocationRepository locationRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${otp.email.verification:false}")
    private boolean otpEmailVerification;

    @Value("${otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${otp.code.length:6}")
    private int otpCodeLength;

    public UserService(IUserRepository userRepository,
                       ILocationRepository locationRepository,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByUserEmailAndDeletedFalse(request.getUserEmail())) {
            throw new DuplicateResourceException("User with this email already exists");
        }
        if (userRepository.existsByUserNameAndDeletedFalse(request.getUserName())) {
            throw new DuplicateResourceException("User with this username already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUserName(request.getUserName());
        user.setUserEmail(request.getUserEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        AccountType accountType = AccountType.valueOf(request.getAccountType().toUpperCase());
        user.setAccountType(accountType);
        user.setRole(deriveRole(accountType));
        user.setEnabled(true);

        if (request.getVillageId() != null && !request.getVillageId().isBlank()) {
            Location village = locationRepository.findById(UUID.fromString(request.getVillageId()))
                    .orElseThrow(() -> new BadRequestException("Village not found"));
            if (village.getLocationType() != auca.ac.rw.FinanceTracker.enums.LocationType.VILLAGE) {
                throw new BadRequestException("Selected location must be a village");
            }
            user.setVillage(village);
        }

        user = userRepository.save(user);
        log.info("User registered: {}", user.getUserEmail());

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getUserEmail(), user.getRole().name());
        return new AuthResponse(token, convertToDTO(user));
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = findByUsernameOrEmail(request.getUserIdentifier());

        User user = userOpt.orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new BadRequestException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (otpEmailVerification) {
            if (request.getOtp() == null || request.getOtp().isBlank()) {
                String otp = generateOtpCode();
                user.setLoginOtp(otp);
                user.setLoginOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
                userRepository.save(user);

                EmailDetails details = new EmailDetails();
                details.setRecipient(user.getUserEmail());
                details.setSubject("Your FinanceTracker login OTP");
                details.setMsgBody("Your OTP code is: " + otp + "\n\nIt expires in " + otpExpiryMinutes + " minutes.");

                String mailStatus = emailService.sendSimpleMail(details);
                if (mailStatus == null || mailStatus.toLowerCase().startsWith("error")) {
                    throw new BadRequestException("Failed to send OTP email. Please check mail configuration.");
                }

                log.info("OTP sent for login: {}", user.getUserEmail());
                return new AuthResponse(null, null);
            }

            if (user.getLoginOtp() == null || user.getLoginOtpExpiry() == null
                    || user.getLoginOtpExpiry().isBefore(LocalDateTime.now())
                    || !user.getLoginOtp().equals(request.getOtp().trim())) {
                throw new BadCredentialsException("Invalid or expired OTP");
            }

            user.setLoginOtp(null);
            user.setLoginOtpExpiry(null);
            userRepository.save(user);
        }

        log.info("User logged in: {}", user.getUserEmail());
        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getUserEmail(), user.getRole().name());
        return new AuthResponse(token, convertToDTO(user));
    }

    private String generateOtpCode() {
        int length = Math.max(4, otpCodeLength);
        int bound = (int) Math.pow(10, length);
        int min = (int) Math.pow(10, length - 1);
        int value = new Random().nextInt(bound - min) + min;
        return String.valueOf(value);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByUserEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        log.info("Password reset requested for: {}", email);
        // The caller (controller) should trigger the email send with this token
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        if (!user.getUserEmail().equals(request.getEmail())) {
            throw new BadRequestException("Invalid reset token for this email");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset completed for: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    @Override
    @Transactional
    public FinancialProfileDTO setupFinancialProfile(UUID userId, FinancialProfileRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getMonthlyIncome() != null) {
            user.setMonthlyIncome(request.getMonthlyIncome());
        }
        if (request.getPreferredCurrency() != null) {
            user.setPreferredCurrency(Currency.valueOf(request.getPreferredCurrency().toUpperCase()));
        }
        if (request.getFinancialGoal() != null) {
            user.setFinancialGoal(request.getFinancialGoal());
        }
        if (request.getBudgetingPreference() != null) {
            user.setBudgetingPreference(BudgetingPreference.valueOf(request.getBudgetingPreference().toUpperCase()));
        }
        if (request.getPreferredBudgetFrequency() != null) {
            user.setPreferredBudgetFrequency(RecurrenceFrequency.valueOf(request.getPreferredBudgetFrequency().toUpperCase()));
        }
        user.setProfileCompleted(true);
        userRepository.save(user);
        log.info("Financial profile set up for user: {}", userId);
        return toFinancialProfileDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialProfileDTO getFinancialProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toFinancialProfileDTO(user);
    }

    private FinancialProfileDTO toFinancialProfileDTO(User user) {
        return new FinancialProfileDTO(
                user.getMonthlyIncome(),
                user.getPreferredCurrency() != null ? user.getPreferredCurrency().name() : null,
                user.getFinancialGoal(),
                user.getBudgetingPreference() != null ? user.getBudgetingPreference().name() : null,
                user.getPreferredBudgetFrequency() != null ? user.getPreferredBudgetFrequency().name() : null,
                user.isProfileCompleted()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllActive().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(UUID userId, UUID authenticatedUserId, SignupRequest request) {
        if (!userId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("You can only update your own profile");
        }

        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUserEmail(request.getUserEmail());

        user = userRepository.save(user);
        log.info("User updated: {}", user.getUserEmail());
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId, UUID authenticatedUserId) {
        if (!userId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("You can only delete your own account");
        }

        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.softDelete();
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User soft-deleted: {}", user.getUserEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByProvince(String identifier) {
        return userRepository.findUsersByProvinceCodeOrName(identifier)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private Optional<User> findByUsernameOrEmail(String identifier) {
        Optional<User> user = userRepository.findByUserNameAndDeletedFalse(identifier);
        if (user.isEmpty()) {
            user = userRepository.findByUserEmailAndDeletedFalse(identifier);
        }
        return user;
    }

    private RoleType deriveRole(AccountType accountType) {
        return switch (accountType) {
            case PERSONAL -> RoleType.PARTICULAR;
            case BUSINESS -> RoleType.ACCOUNTANT;
            case COMPANY  -> RoleType.TREASURER;
            default       -> RoleType.PARTICULAR;
        };
    }

    private UserDTO convertToDTO(User user) {
        String locationPath = null;
        if (user.getVillage() != null) {
            locationPath = buildLocationPath(user.getVillage());
        }
        return new UserDTO(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserName(),
                user.getUserEmail(),
                user.getAccountType(),
                user.getRole(),
                user.isEnabled(),
                user.getMonthlyIncome(),
                user.getPreferredCurrency() != null ? user.getPreferredCurrency().name() : null,
                user.getFinancialGoal(),
                user.getBudgetingPreference() != null ? user.getBudgetingPreference().name() : null,
                user.getPreferredBudgetFrequency() != null ? user.getPreferredBudgetFrequency().name() : null,
                user.isProfileCompleted(),
                user.getVillage() != null ? user.getVillage().getLocationId().toString() : null,
                user.getVillage() != null ? user.getVillage().getName() : null,
                locationPath
        );
    }

    private String buildLocationPath(Location location) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        Location current = location;
        while (current != null) {
            parts.add(current.getName());
            current = current.getParent();
        }
        java.util.Collections.reverse(parts);
        return String.join(" > ", parts);
    }
}
