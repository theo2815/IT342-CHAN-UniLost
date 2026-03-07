package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.ChatEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends MongoRepository<ChatEntity, String> {

    List<ChatEntity> findByItemId(String itemId);

    List<ChatEntity> findByFinderIdOrOwnerId(String finderId, String ownerId);

    Optional<ChatEntity> findByItemIdAndFinderIdAndOwnerId(String itemId, String finderId, String ownerId);

    // TODO: [Phase 6] Add query ordered by updatedAt for chat list
}
