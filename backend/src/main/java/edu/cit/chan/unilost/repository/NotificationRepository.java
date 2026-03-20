package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEntity, String> {

    Page<NotificationEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<NotificationEntity> findByUserIdAndIsReadFalse(String userId);

    long countByUserIdAndIsReadFalse(String userId);

    void deleteByUserIdAndCreatedAtBefore(String userId, LocalDateTime before);
}
