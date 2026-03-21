package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.dto.NotificationDTO;
import edu.cit.chan.unilost.entity.NotificationEntity;
import edu.cit.chan.unilost.exception.ResourceNotFoundException;
import edu.cit.chan.unilost.repository.NotificationRepository;
import edu.cit.chan.unilost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Query Methods ──────────────────────────────────────

    public Page<NotificationDTO> getNotifications(String email, Pageable pageable) {
        String userId = resolveUserId(email);
        Page<NotificationEntity> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationDTO> dtos = page.getContent().stream().map(this::toDTO).toList();
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    public long getUnreadCount(String email) {
        String userId = resolveUserId(email);
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // ── Mutation Methods ───────────────────────────────────

    public NotificationDTO markAsRead(String notificationId, String email) {
        String userId = resolveUserId(email);
        NotificationEntity entity = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        entity.setRead(true);
        return toDTO(notificationRepository.save(entity));
    }

    public void markAllAsRead(String email) {
        String userId = resolveUserId(email);
        List<NotificationEntity> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        if (!unread.isEmpty()) {
            unread.forEach(n -> n.setRead(true));
            notificationRepository.saveAll(unread);
        }
    }

    public void deleteNotification(String notificationId, String email) {
        String userId = resolveUserId(email);
        NotificationEntity entity = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notificationRepository.delete(entity);
    }

    // ── Trigger Methods (called from other services) ──────

    public void notifyClaimReceived(String finderUserId, String claimantName, String itemTitle, String claimId) {
        createAndPush(finderUserId, "CLAIM_RECEIVED",
                "New claim on your item",
                claimantName + " claimed your '" + itemTitle + "'. Review their claim to verify ownership.",
                claimId);
    }

    public void notifyClaimAccepted(String claimantUserId, String itemTitle, String claimId) {
        createAndPush(claimantUserId, "CLAIM_ACCEPTED",
                "Your claim was accepted!",
                "Great news! Your claim on '" + itemTitle + "' was accepted. Please arrange a handover.",
                claimId);
    }

    public void notifyClaimRejected(String claimantUserId, String itemTitle, String claimId) {
        createAndPush(claimantUserId, "CLAIM_REJECTED",
                "Claim not approved",
                "Your claim on '" + itemTitle + "' was not approved by the poster. You can browse other items.",
                claimId);
    }

    public void notifyClaimAutoRejected(String claimantUserId, String itemTitle, String claimId) {
        createAndPush(claimantUserId, "CLAIM_REJECTED",
                "Claim auto-rejected",
                "Your claim on '" + itemTitle + "' was auto-rejected because another claim was accepted.",
                claimId);
    }

    public void notifyNewMessage(String recipientUserId, String senderName, String chatId) {
        createAndPush(recipientUserId, "NEW_MESSAGE",
                "New message from " + senderName,
                senderName + " sent you a message. Tap to view the conversation.",
                chatId);
    }

    public void notifyItemFlagged(String reporterUserId, String itemTitle, String itemId) {
        createAndPush(reporterUserId, "ITEM_FLAGGED",
                "Your item was flagged",
                "Your listing '" + itemTitle + "' has been flagged for review by a moderator.",
                itemId);
    }

    public void notifyItemMarkedReturned(String ownerUserId, String finderName,
                                          String itemTitle, String chatId) {
        createAndPush(ownerUserId, "ITEM_MARKED_RETURNED",
                "Item marked as returned",
                finderName + " has returned your '" + itemTitle
                        + "'. Please confirm that you have received it.",
                chatId);
    }

    public void notifyItemReturned(String userId, String itemTitle,
                                    int karmaAwarded, String chatId) {
        createAndPush(userId, "ITEM_RETURNED",
                "Item successfully returned!",
                "The return of '" + itemTitle + "' is confirmed! You earned +"
                        + karmaAwarded + " karma points.",
                chatId);
    }

    // ── Internal Helpers ───────────────────────────────────

    private void createAndPush(String userId, String type, String title, String message, String linkId) {
        NotificationEntity entity = new NotificationEntity();
        entity.setUserId(userId);
        entity.setType(type);
        entity.setTitle(title);
        entity.setMessage(message);
        entity.setLinkId(linkId);
        entity.setRead(false);
        entity.setCreatedAt(LocalDateTime.now());

        NotificationEntity saved = notificationRepository.save(entity);
        NotificationDTO dto = toDTO(saved);

        // Push via WebSocket to user-specific queue
        // The principal is the user's email (set by WebSocketAuthInterceptor)
        try {
            String email = userRepository.findById(userId)
                    .map(u -> u.getEmail())
                    .orElse(null);
            if (email != null) {
                messagingTemplate.convertAndSendToUser(email, "/queue/notifications", dto);
            }
        } catch (Exception e) {
            log.warn("Failed to push notification via WebSocket to user {}: {}", userId, e.getMessage());
        }
    }

    private NotificationDTO toDTO(NotificationEntity entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setLinkId(entity.getLinkId());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private String resolveUserId(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }
}
