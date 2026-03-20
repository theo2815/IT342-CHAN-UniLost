package edu.cit.chan.unilost.dto;

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
}
