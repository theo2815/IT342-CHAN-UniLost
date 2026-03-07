package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a notification sent to a user.
 *
 * Relationships (MongoDB references via ID):
 * - Many-to-One: Notification → User (user_id)
 */
// TODO: [Phase 8] Implement push notifications (FCM / WebSocket)
// TODO: [Phase 8] Add notification grouping and batch read
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class NotificationEntity {

    @Id
    private String id;

    /** Reference to users.id — the recipient */
    private String userId;

    /** MESSAGE, CLAIM_UPDATE, HANDOVER_SUCCESS */
    private String type;

    private String content;

    /** Generic reference ID — points to the relevant chat, claim, or handover */
    private String linkId;

    private boolean isRead = false;

    private LocalDateTime createdAt;
}
