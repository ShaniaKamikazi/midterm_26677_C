package auca.ac.rw.FinanceTracker.repository;

import auca.ac.rw.FinanceTracker.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserSettingsRepository extends JpaRepository<UserSettings, UUID> {

    Optional<UserSettings> findByUserUserId(UUID userId);
}
