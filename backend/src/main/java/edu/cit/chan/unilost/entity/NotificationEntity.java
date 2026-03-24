package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
@CompoundIndex(name = "idx_user_read_created", def = "{'userId': 1, 'isRead': 1, 'createdAt': -1}")
public class NotificationEntity {

    @Id
    private String id;

    /** Reference to users.id — the recipient */
    @Indexed
    private String userId;

    /** CLAIM_RECEIVED, CLAIM_ACCEPTED, CLAIM_REJECTED, NEW_MESSAGE, ITEM_FLAGGED */
    private String type;

    /** Short notification title */
    private String title;

    /** Full notification body text */
    private String message;

    /** Generic reference ID — points to the relevant claim, item, or chat */
    private String linkId;

    private boolean isRead = false;

    private LocalDateTime createdAt;
}
