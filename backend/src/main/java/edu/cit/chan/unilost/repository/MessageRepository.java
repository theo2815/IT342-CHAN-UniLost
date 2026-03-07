package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.MessageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageEntity, String> {

    List<MessageEntity> findByChatIdOrderByCreatedAtAsc(String chatId);

    List<MessageEntity> findByChatIdAndIsReadFalse(String chatId);

    long countByChatIdAndIsReadFalseAndSenderIdNot(String chatId, String userId);

    // TODO: [Phase 6] Add pagination for message history
}
