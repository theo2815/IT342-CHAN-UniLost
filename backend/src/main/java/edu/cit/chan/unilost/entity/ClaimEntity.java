package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a claim attempt on a found item.
 *
 * Relationships (MongoDB references via ID):
 * - Many-to-One: Claim → Item (item_id)
 * - Many-to-One: Claim → User (claimant_id)
 * - Many-to-One: Claim → User (finder_id)
 * - One-to-One: Claim → Handover (resolved via HandoverEntity.claimId)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "claims")
public class ClaimEntity {

    @Id
    private String id;

    /** Reference to items.id — the item being claimed */
    @Indexed
    private String itemId;

    /** Reference to users.id — the user making the claim */
    @Indexed
    private String claimantId;

    /** Reference to users.id — the finder/poster of the item */
    @Indexed
    private String finderId;

    /** The claimant's answer to the item's secret verification question */
    private String providedAnswer;

    /** Free-text message from the claimant */
    private String message;

    /** PENDING, ACCEPTED, REJECTED, CANCELLED */
    private ClaimStatus status = ClaimStatus.PENDING;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
