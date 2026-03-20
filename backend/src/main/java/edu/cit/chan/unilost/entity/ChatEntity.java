package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a chat thread between a finder and a claimant for a specific item.
 *
 * Relationships (MongoDB references via ID):
 * - Many-to-One: Chat → Item (item_id)
 * - Many-to-One: Chat → User (finder_id)
 * - Many-to-One: Chat → User (owner_id)
 * - One-to-Many: Chat → Messages (resolved via MessageEntity.chatId)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chats")
public class ChatEntity {

    @Id
    private String id;

    /** Reference to items.id — the item this chat is about */
    private String itemId;

    /** Reference to claims.id — the claim that triggered this chat */
    private String claimId;

    /** Reference to users.id — the person who found/reported the item */
    private String finderId;

    /** Reference to users.id — the person claiming ownership */
    private String ownerId;

    /** Preview of the last message for chat list display */
    private String lastMessagePreview;

    private LocalDateTime lastMessageAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
