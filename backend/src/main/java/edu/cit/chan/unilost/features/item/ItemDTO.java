package edu.cit.chan.unilost.features.item;

import edu.cit.chan.unilost.features.campus.CampusDTO;
import edu.cit.chan.unilost.features.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {

    private String id;
    private String title;
    private String description;
    private String type;
    private String status;
    private String category;
    private String location;
    private Double latitude;
    private Double longitude;
    private List<String> imageUrls;
    private String secretDetailQuestion;
    private LocalDateTime dateLostFound;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Resolved references
    private String reporterId;
    private String campusId;
    private UserDTO reporter;
    private CampusDTO campus;

    // Flagging info (admin views)
    private int flagCount;
    private List<String> flagReasons;
    private List<FlagDetail> flagDetails;

    // Admin moderation action (visible to owner + admin)
    private String adminActionType;
    private String adminActionReason;
    private LocalDateTime adminActionAt;

    // Owner appeal lifecycle
    private String appealStatus;
    private String appealText;
    private LocalDateTime appealedAt;
    private LocalDateTime appealResolvedAt;
    private String appealAdminNote;

    // Viewer-specific report state (only set on single-item detail fetches with auth)
    private Boolean viewerHasFlagged;
    private FlagDetail viewerFlagDetail;

    // True only on the owner/admin view of a soft-deleted item — lets the UI
    // explain that the item is gone instead of rendering a blank detail page.
    private Boolean isDeleted;
}
