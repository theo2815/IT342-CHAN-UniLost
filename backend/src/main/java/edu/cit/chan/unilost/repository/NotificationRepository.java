package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.NotificationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEntity, String> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    List<NotificationEntity> findByUserIdAndIsReadFalse(String userId);

    long countByUserIdAndIsReadFalse(String userId);

    // TODO: [Phase 8] Add batch mark-as-read operation
    // TODO: [Phase 8] Add auto-cleanup for old notifications
}
