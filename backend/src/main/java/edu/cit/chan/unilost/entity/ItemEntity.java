package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items")
public class ItemEntity {

    @Id
    private String id;

    @Version
    private Long version;

    @Indexed
    private String reporterId;

    @Indexed
    private String campusId;

    private String title;

    private String type;

    private String status = "ACTIVE";

    private String category;

    private String location;

    private List<String> imageUrls = new ArrayList<>();

    private String secretDetailQuestion;

    private String description;

    private LocalDateTime dateLostFound;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean isDeleted = false;

    private LocalDateTime deletedAt;
}
