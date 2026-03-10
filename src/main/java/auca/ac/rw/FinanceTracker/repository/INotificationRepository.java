package auca.ac.rw.FinanceTracker.repository;

import auca.ac.rw.FinanceTracker.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId);

    Page<Notification> findByUserUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    List<Notification> findByUserUserIdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(UUID userId);

    long countByUserUserIdAndReadFalseAndDeletedFalse(UUID userId);

    Optional<Notification> findByNotificationIdAndDeletedFalse(UUID notificationId);
}
