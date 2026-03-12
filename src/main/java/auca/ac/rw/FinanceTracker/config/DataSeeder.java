package auca.ac.rw.FinanceTracker.config;

import auca.ac.rw.FinanceTracker.enums.AccountType;
import auca.ac.rw.FinanceTracker.enums.RoleType;
import auca.ac.rw.FinanceTracker.model.User;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
    }

    private void seedAdmin() {
        if (userRepository.existsByUserEmailAndDeletedFalse("kamikazishania@gmail.com")) {
            return;
        }

        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setUserName("admin");
        admin.setUserEmail("kamikazishania@gmail.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(RoleType.ADMIN);
        admin.setAccountType(AccountType.PERSONAL);
        admin.setEnabled(true);

        userRepository.save(admin);
        log.info("Admin user seeded: kamikazishania@gmail.com");
    }
}
