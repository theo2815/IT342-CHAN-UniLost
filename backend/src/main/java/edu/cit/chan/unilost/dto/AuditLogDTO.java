package edu.cit.chan.unilost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private String id;
    private String action;
    private String targetType;
    private String targetId;
    private String adminEmail;
    private String details;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
