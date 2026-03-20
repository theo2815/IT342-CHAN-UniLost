package edu.cit.chan.unilost.config;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoIndexInitializer {

    private final MongoTemplate mongoTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        var db = mongoTemplate.getDb();

        // Items collection — indexes for admin campus-scoped queries
        var itemsCollection = db.getCollection("items");
        itemsCollection.createIndex(
                Indexes.compoundIndex(Indexes.ascending("campusId"), Indexes.ascending("status"), Indexes.ascending("isDeleted")),
                new IndexOptions().name("idx_campus_status_deleted"));
        itemsCollection.createIndex(
                Indexes.compoundIndex(Indexes.ascending("campusId"), Indexes.ascending("isDeleted"), Indexes.ascending("flagCount")),
                new IndexOptions().name("idx_campus_deleted_flagged"));
        // Map query index — supports getMapItems() filtering on isDeleted + status + latitude existence
        itemsCollection.createIndex(
                Indexes.compoundIndex(Indexes.ascending("isDeleted"), Indexes.ascending("status"), Indexes.ascending("latitude"), Indexes.descending("createdAt")),
                new IndexOptions().name("idx_map_items").sparse(true));

        // Users collection — index for campus-scoped user queries
        var usersCollection = db.getCollection("users");
        usersCollection.createIndex(
                Indexes.compoundIndex(Indexes.ascending("universityTag"), Indexes.ascending("accountStatus")),
                new IndexOptions().name("idx_campus_account_status"));

        // Chats collection
        var chatsCollection = db.getCollection("chats");
        // H5: Unique compound index to prevent duplicate chats for same item+finder+owner
        chatsCollection.createIndex(
                Indexes.compoundIndex(Indexes.ascending("itemId"), Indexes.ascending("finderId"), Indexes.ascending("ownerId")),
                new IndexOptions().name("idx_item_finder_owner").unique(true));
        // Index for user chat list query (findByFinderIdOrOwnerId ordered by lastMessageAt)
        chatsCollection.createIndex(
                Indexes.compoundIndex(Indexes.ascending("finderId"), Indexes.descending("lastMessageAt")),
                new IndexOptions().name("idx_finder_lastmsg"));
        chatsCollection.createIndex(
                Indexes.compoundIndex(Indexes.ascending("ownerId"), Indexes.descending("lastMessageAt")),
                new IndexOptions().name("idx_owner_lastmsg"));
        // Index for claim lookup
        chatsCollection.createIndex(
                Indexes.ascending("claimId"),
                new IndexOptions().name("idx_claimId"));

        // Notifications collection
        var notificationsCollection = db.getCollection("notifications");
        notificationsCollection.createIndex(
                Indexes.compoundIndex(Indexes.ascending("userId"), Indexes.ascending("isRead"), Indexes.descending("createdAt")),
                new IndexOptions().name("idx_user_read_created"));

        // Messages collection
        var messagesCollection = db.getCollection("messages");
        // Index for paginated message retrieval by chat
        messagesCollection.createIndex(
                Indexes.compoundIndex(Indexes.ascending("chatId"), Indexes.descending("createdAt")),
                new IndexOptions().name("idx_chat_created"));
        // Index for unread count queries
        messagesCollection.createIndex(
                Indexes.compoundIndex(Indexes.ascending("chatId"), Indexes.ascending("isRead"), Indexes.ascending("senderId")),
                new IndexOptions().name("idx_chat_read_sender"));
    }
}
