package edu.cit.chan.unilost.features.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageEntity, String> {

    Page<MessageEntity> findByChatIdOrderByCreatedAtDesc(String chatId, Pageable pageable);

    List<MessageEntity> findByChatIdAndIsReadFalseAndSenderIdNot(String chatId, String userId);

    long countByChatIdAndIsReadFalseAndSenderIdNot(String chatId, String userId);
}
