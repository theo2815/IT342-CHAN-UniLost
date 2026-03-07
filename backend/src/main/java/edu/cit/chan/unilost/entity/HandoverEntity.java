package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a handover transaction after a claim is verified.
 * Both the finder and owner must confirm for the handover to complete.
 *
 * Relationships (MongoDB references via ID):
 * - One-to-One: Handover → Claim (claim_id)
 * - Many-to-One: Handover → Item (item_id)
 */
// TODO: [Phase 7] Update karma_score for both parties on completion
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "handovers")
public class HandoverEntity {

    @Id
    private String id;

    /** Reference to claims.id — the verified claim this handover fulfills */
    private String claimId;

    /** Reference to items.id — the item being handed over */
    private String itemId;

    /** Whether the finder has confirmed the handover */
    private boolean finderConfirmed = false;

    /** Whether the owner has confirmed the handover */
    private boolean ownerConfirmed = false;

    /** Timestamp when both parties confirmed — null until complete */
    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
}
