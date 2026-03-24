package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a single message within a chat thread.
 *
 * Relationships (MongoDB references via ID):
 * - Many-to-One: Message → Chat (chat_id)
 * - Many-to-One: Message → User (sender_id)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class MessageEntity {

    @Id
    private String id;

    /** Reference to chats.id */
    @Indexed
    private String chatId;

    /** Reference to users.id — who sent this message. Null for system messages. */
    private String senderId;

    private String content;

    /** Message type — defaults to TEXT for backward compat with existing messages */
    private MessageType type = MessageType.TEXT;

    /** Structured data for non-TEXT messages (claim details, karma amounts, etc.) */
    private Map<String, Object> metadata;

    private boolean isRead = false;

    private LocalDateTime createdAt;
}
