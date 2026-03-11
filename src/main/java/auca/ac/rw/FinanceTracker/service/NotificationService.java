package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.NotificationDTO;
import auca.ac.rw.FinanceTracker.enums.NotificationType;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.exception.UnauthorizedException;
import auca.ac.rw.FinanceTracker.model.Notification;
import auca.ac.rw.FinanceTracker.model.User;
import auca.ac.rw.FinanceTracker.repository.INotificationRepository;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService implements INotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final INotificationRepository notificationRepository;
    private final IUserRepository userRepository;

    public NotificationService(INotificationRepository notificationRepository, IUserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public NotificationDTO createNotification(UUID userId, String title, String message, NotificationType type) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        notification = notificationRepository.save(notification);
        log.info("Notification created for user {}: {}", userId, title);
        return toDTO(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotifications(UUID userId) {
        return notificationRepository.findByUserUserIdAndDeletedFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsPaginated(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserUserIdAndDeletedFalse(userId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserUserIdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserUserIdAndReadFalseAndDeletedFalse(userId);
    }

    @Override
    @Transactional
    public NotificationDTO markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByNotificationIdAndDeletedFalse(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this notification");
        }

        notification.setRead(true);
        notification = notificationRepository.save(notification);
        return toDTO(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unread = notificationRepository
                .findByUserUserIdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(userId);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
        log.info("Marked all notifications as read for user {}", userId);
    }

    private NotificationDTO toDTO(Notification n) {
        return new NotificationDTO(
                n.getNotificationId(),
                n.getTitle(),
                n.getMessage(),
                n.getType().name(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
