package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.NotificationDTO;
import auca.ac.rw.FinanceTracker.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface INotificationService {

    NotificationDTO createNotification(UUID userId, String title, String message, NotificationType type);

    List<NotificationDTO> getNotifications(UUID userId);

    Page<NotificationDTO> getNotificationsPaginated(UUID userId, Pageable pageable);

    List<NotificationDTO> getUnreadNotifications(UUID userId);

    long getUnreadCount(UUID userId);

    NotificationDTO markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);
}
