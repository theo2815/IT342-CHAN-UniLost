package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.dto.ChatDTO;
import edu.cit.chan.unilost.dto.MessageDTO;
import edu.cit.chan.unilost.entity.ChatEntity;
import edu.cit.chan.unilost.entity.ItemEntity;
import edu.cit.chan.unilost.entity.MessageEntity;
import edu.cit.chan.unilost.entity.UserEntity;
import edu.cit.chan.unilost.exception.ForbiddenException;
import edu.cit.chan.unilost.exception.ResourceNotFoundException;
import edu.cit.chan.unilost.repository.ChatRepository;
import edu.cit.chan.unilost.repository.ItemRepository;
import edu.cit.chan.unilost.repository.MessageRepository;
import edu.cit.chan.unilost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MongoTemplate mongoTemplate;
    private final NotificationService notificationService;

    /**
     * Auto-create a chat room when a claim is submitted.
     * If a chat already exists for the same item+finder+owner, return it instead.
     */
    public ChatEntity createChatForClaim(String itemId, String claimId, String finderId, String ownerId) {
        Optional<ChatEntity> existing = chatRepository.findByItemIdAndFinderIdAndOwnerId(itemId, finderId, ownerId);
        if (existing.isPresent()) {
            return existing.get();
        }

        ChatEntity chat = new ChatEntity();
        chat.setItemId(itemId);
        chat.setClaimId(claimId);
        chat.setFinderId(finderId);
        chat.setOwnerId(ownerId);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setUpdatedAt(LocalDateTime.now());
        chat.setLastMessageAt(LocalDateTime.now());
        return chatRepository.save(chat);
    }

    /**
     * Get all chats for the current user, ordered by most recent message.
     */
    public List<ChatDTO> getMyChats(String currentEmail) {
        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<ChatEntity> chats = chatRepository.findByFinderIdOrOwnerIdOrderByLastMessageAtDesc(
                currentUser.getId(), currentUser.getId());

        if (chats.isEmpty()) return List.of();

        // Batch-load all referenced entities to avoid N+1
        Set<String> userIds = new HashSet<>();
        Set<String> itemIds = new HashSet<>();
        chats.forEach(c -> {
            userIds.add(c.getFinderId());
            userIds.add(c.getOwnerId());
            if (c.getItemId() != null) itemIds.add(c.getItemId());
        });

        Map<String, UserEntity> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        Map<String, ItemEntity> itemsById = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(ItemEntity::getId, Function.identity()));

        return chats.stream().map(chat -> convertToDTO(chat, currentUser.getId(), usersById, itemsById)).toList();
    }

    /**
     * Get a specific chat by ID, with authorization check.
     */
    public ChatDTO getChatById(String chatId, String currentEmail) {
        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        verifyParticipant(chat, currentUser.getId());

        Map<String, UserEntity> usersById = userRepository.findAllById(
                List.of(chat.getFinderId(), chat.getOwnerId())).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        Map<String, ItemEntity> itemsById = chat.getItemId() != null
                ? itemRepository.findAllById(List.of(chat.getItemId())).stream()
                .collect(Collectors.toMap(ItemEntity::getId, Function.identity()))
                : Map.of();

        return convertToDTO(chat, currentUser.getId(), usersById, itemsById);
    }

    /**
     * Get paginated messages for a chat, with authorization check.
     */
    public Page<MessageDTO> getMessages(String chatId, String currentEmail, Pageable pageable) {
        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        verifyParticipant(chat, currentUser.getId());

        Page<MessageEntity> messagePage = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);

        // Batch-load sender names
        Set<String> senderIds = messagePage.getContent().stream()
                .map(MessageEntity::getSenderId).collect(Collectors.toSet());
        Map<String, UserEntity> sendersById = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        List<MessageDTO> dtos = messagePage.getContent().stream()
                .map(msg -> convertMessageToDTO(msg, sendersById))
                .toList();

        return new PageImpl<>(dtos, pageable, messagePage.getTotalElements());
    }

    /**
     * Send a message in a chat. Broadcasts via WebSocket.
     */
    public MessageDTO sendMessage(String chatId, String currentEmail, String content) {
        UserEntity sender = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        verifyParticipant(chat, sender.getId());

        String trimmedContent = content.trim();
        if (trimmedContent.length() > 2000) {
            throw new IllegalArgumentException("Message cannot exceed 2000 characters");
        }

        MessageEntity message = new MessageEntity();
        message.setChatId(chatId);
        message.setSenderId(sender.getId());
        message.setContent(trimmedContent);
        message.setCreatedAt(LocalDateTime.now());
        MessageEntity saved = messageRepository.save(message);

        // Update chat's last message preview
        chat.setLastMessagePreview(trimmedContent.length() > 100 ? trimmedContent.substring(0, 100) + "..." : trimmedContent);
        chat.setLastMessageAt(LocalDateTime.now());
        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);

        MessageDTO dto = convertMessageToDTO(saved, Map.of(sender.getId(), sender));

        // Broadcast to the chat topic for real-time delivery
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, dto);

        // Notify the other participant about the new message
        String recipientId = chat.getFinderId().equals(sender.getId()) ? chat.getOwnerId() : chat.getFinderId();
        notificationService.notifyNewMessage(recipientId, sender.getFullName(), chatId);

        return dto;
    }

    /**
     * Mark all unread messages in a chat as read (for the current user).
     */
    public void markAsRead(String chatId, String currentEmail) {
        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        verifyParticipant(chat, currentUser.getId());

        List<MessageEntity> unread = messageRepository.findByChatIdAndIsReadFalseAndSenderIdNot(
                chatId, currentUser.getId());
        if (!unread.isEmpty()) {
            unread.forEach(msg -> msg.setRead(true));
            messageRepository.saveAll(unread);
        }
    }

    /**
     * Get total unread message count across all chats for the current user.
     * Uses a single MongoDB aggregation instead of N+1 queries.
     */
    public long getTotalUnreadCount(String currentEmail) {
        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String userId = currentUser.getId();

        // Get all chat IDs the user participates in
        List<ChatEntity> chats = chatRepository.findByFinderIdOrOwnerIdOrderByLastMessageAtDesc(userId, userId);
        if (chats.isEmpty()) return 0;

        List<String> chatIds = chats.stream().map(ChatEntity::getId).toList();

        // Single aggregation: count all unread messages not sent by the current user across all their chats
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("chatId").in(chatIds)
                        .and("isRead").is(false)
                        .and("senderId").ne(userId)),
                Aggregation.count().as("total")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "messages", Map.class);
        Map firstResult = results.getUniqueMappedResult();
        if (firstResult == null) return 0;
        return ((Number) firstResult.get("total")).longValue();
    }

    // --- Private helpers ---

    private void verifyParticipant(ChatEntity chat, String userId) {
        if (!chat.getFinderId().equals(userId) && !chat.getOwnerId().equals(userId)) {
            throw new ForbiddenException("You are not a participant in this chat");
        }
    }

    private ChatDTO convertToDTO(ChatEntity chat, String currentUserId,
                                  Map<String, UserEntity> usersById,
                                  Map<String, ItemEntity> itemsById) {
        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setItemId(chat.getItemId());
        dto.setClaimId(chat.getClaimId());
        dto.setFinderId(chat.getFinderId());
        dto.setOwnerId(chat.getOwnerId());
        dto.setLastMessagePreview(chat.getLastMessagePreview());
        dto.setLastMessageAt(chat.getLastMessageAt());
        dto.setCreatedAt(chat.getCreatedAt());

        // Resolve item
        ItemEntity item = itemsById.get(chat.getItemId());
        if (item != null) {
            dto.setItemTitle(item.getTitle());
            dto.setItemImageUrl(item.getImageUrls() != null && !item.getImageUrls().isEmpty()
                    ? item.getImageUrls().get(0) : null);
        }

        // Resolve participant names (privacy: use fullName since both are claim participants)
        UserEntity finder = usersById.get(chat.getFinderId());
        UserEntity owner = usersById.get(chat.getOwnerId());
        if (finder != null) dto.setFinderName(finder.getFullName());
        if (owner != null) dto.setOwnerName(owner.getFullName());

        // Determine "other participant" relative to current user
        if (currentUserId.equals(chat.getFinderId())) {
            dto.setOtherParticipantId(chat.getOwnerId());
            dto.setOtherParticipantName(owner != null ? owner.getFullName() : "Unknown");
        } else {
            dto.setOtherParticipantId(chat.getFinderId());
            dto.setOtherParticipantName(finder != null ? finder.getFullName() : "Unknown");
        }

        // Unread count
        dto.setUnreadCount(messageRepository.countByChatIdAndIsReadFalseAndSenderIdNot(
                chat.getId(), currentUserId));

        return dto;
    }

    private MessageDTO convertMessageToDTO(MessageEntity msg, Map<String, UserEntity> sendersById) {
        MessageDTO dto = new MessageDTO();
        dto.setId(msg.getId());
        dto.setChatId(msg.getChatId());
        dto.setSenderId(msg.getSenderId());
        dto.setContent(msg.getContent());
        dto.setRead(msg.isRead());
        dto.setCreatedAt(msg.getCreatedAt());

        UserEntity sender = sendersById.get(msg.getSenderId());
        if (sender != null) {
            dto.setSenderName(sender.getFullName());
        }

        return dto;
    }
}
