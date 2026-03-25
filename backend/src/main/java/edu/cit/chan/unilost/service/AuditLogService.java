package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.dto.AuditLogDTO;
import edu.cit.chan.unilost.entity.AuditLogEntity;
import edu.cit.chan.unilost.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String targetType, String targetId,
                    String adminEmail, String details, Map<String, Object> metadata) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setAction(action);
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        entity.setAdminEmail(adminEmail);
        entity.setDetails(details);
        entity.setMetadata(metadata);
        entity.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(entity);
    }

    public Page<AuditLogDTO> getAuditLogs(String action, String targetType, Pageable pageable) {
        Page<AuditLogEntity> page;

        if (action != null && !action.isEmpty() && targetType != null && !targetType.isEmpty()) {
            page = auditLogRepository.findByActionAndTargetTypeOrderByCreatedAtDesc(action, targetType, pageable);
        } else if (action != null && !action.isEmpty()) {
            page = auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
        } else if (targetType != null && !targetType.isEmpty()) {
            page = auditLogRepository.findByTargetTypeOrderByCreatedAtDesc(targetType, pageable);
        } else {
            page = auditLogRepository.findByOrderByCreatedAtDesc(pageable);
        }

        List<AuditLogDTO> dtos = page.getContent().stream().map(this::toDTO).toList();
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    private AuditLogDTO toDTO(AuditLogEntity entity) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(entity.getId());
        dto.setAction(entity.getAction());
        dto.setTargetType(entity.getTargetType());
        dto.setTargetId(entity.getTargetId());
        dto.setAdminEmail(entity.getAdminEmail());
        dto.setDetails(entity.getDetails());
        dto.setMetadata(entity.getMetadata());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
