package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a single message within a chat thread.
 *
 * Relationships (MongoDB references via ID):
 * - Many-to-One: Message → Chat (chat_id)
 * - Many-to-One: Message → User (sender_id)
 */
// TODO: [Phase 6] Add WebSocket broadcast on new message creation
// TODO: [Phase 6] Implement read receipt tracking
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class MessageEntity {

    @Id
    private String id;

    /** Reference to chats.id */
    private String chatId;

    /** Reference to users.id — who sent this message */
    private String senderId;

    private String content;

    private boolean isRead = false;

    private LocalDateTime createdAt;
}
