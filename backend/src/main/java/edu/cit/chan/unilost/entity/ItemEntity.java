package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;

/**
 * Represents a lost or found item report.
 *
 * Relationships (MongoDB references via ID):
 * - Many-to-One: Item → User (reporter_id)
 * - Many-to-One: Item → Campus (campus_id)
 * - One-to-Many: Item → Claims (resolved via ClaimEntity.itemId)
 * - One-to-Many: Item → Chats (resolved via ChatEntity.itemId)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items")
public class ItemEntity {

    @Id
    private String id;

    /** Reference to users.id — the user who reported this item */
    private String reporterId;

    /** Reference to campuses.id — where the item was lost/found */
    private String campusId;

    private String title;

    /** LOST or FOUND */
    private String type;

    /** Category: ELECTRONICS, CLOTHING, DOCUMENTS, ACCESSORIES, BOOKS, KEYS, BAGS, OTHER */
    private String category;

    // TODO: [Phase 4] Add GeoJSON spatial index for location-based queries
    /** GeoJSON Point — exact coordinates where item was lost/found */
    private GeoJsonPoint location;

    // TODO: [Phase 4] Integrate Cloudinary for image upload and blurring
    /** URL to the blurred preview image (shown to non-verified claimants) */
    private String blurredImageUrl;

    /** URL to the original unblurred image (shown after claim verification) */
    private String originalImageUrl;

    /** Secret verification question only the true owner can answer */
    private String secretDetailQuestion;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // TODO: [Phase 4] Implement soft delete logic in service layer
    /** Soft delete flag — item is hidden but not removed from database */
    private boolean isDeleted = false;

    private LocalDateTime deletedAt;
}
