package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a claim attempt on a found item.
 *
 * Relationships (MongoDB references via ID):
 * - Many-to-One: Claim → Item (item_id)
 * - Many-to-One: Claim → User (claimant_id)
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
    private String itemId;

    /** Reference to users.id — the user making the claim */
    private String claimantId;

    /** The claimant's answer to the item's secret verification question */
    private String providedAnswer;

    /** PENDING, ACCEPTED, REJECTED */
    private String status = "PENDING";

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
