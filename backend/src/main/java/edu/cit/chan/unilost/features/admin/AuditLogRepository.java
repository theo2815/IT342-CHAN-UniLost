package edu.cit.chan.unilost.features.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLogEntity, String> {

    Page<AuditLogEntity> findByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLogEntity> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    Page<AuditLogEntity> findByTargetTypeOrderByCreatedAtDesc(String targetType, Pageable pageable);

    Page<AuditLogEntity> findByActionAndTargetTypeOrderByCreatedAtDesc(String action, String targetType, Pageable pageable);
}
