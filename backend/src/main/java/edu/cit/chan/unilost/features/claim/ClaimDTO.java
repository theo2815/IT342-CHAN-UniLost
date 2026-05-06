package edu.cit.chan.unilost.features.claim;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDTO {

    private String id;
    private String status;
    private String providedAnswer;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Resolved item info
    private String itemId;
    private String itemTitle;
    private String itemType;
    private String itemImageUrl;

    // Resolved claimant info
    private String claimantId;
    private String claimantName;
    private String claimantSchool;

    // Resolved finder info
    private String finderId;
    private String finderName;

    // Only populated for finder/admin views
    private String secretDetailQuestion;

    // Chat room associated with this claim
    private String chatId;

    // Handover tracking
    private LocalDateTime finderMarkedReturnedAt;
    private LocalDateTime ownerConfirmedReceivedAt;
}
