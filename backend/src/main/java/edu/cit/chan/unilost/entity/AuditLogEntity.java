package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLogEntity {

    @Id
    private String id;

    @Indexed
    private String action; // UPDATE_ITEM_STATUS, DELETE_ITEM, UPDATE_USER_STATUS, FORCE_COMPLETE_HANDOVER, BULK_UPDATE_ITEMS, BULK_DELETE_ITEMS, BULK_UPDATE_USERS, EXPORT_DATA

    private String targetType; // ITEM, USER, CLAIM

    private String targetId;

    @Indexed
    private String adminEmail;

    private String details;

    private Map<String, Object> metadata;

    @Indexed(direction = org.springframework.data.mongodb.core.index.IndexDirection.DESCENDING)
    private LocalDateTime createdAt;
}
