package edu.cit.chan.unilost.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatDTO {
    private String id;
    private String itemId;
    private String itemTitle;
    private String itemImageUrl;
    private String claimId;
    private String finderId;
    private String finderName;
    private String ownerId;
    private String ownerName;
    private String otherParticipantId;
    private String otherParticipantName;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
    private LocalDateTime createdAt;
}
