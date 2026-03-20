package edu.cit.chan.unilost.controller;

import edu.cit.chan.unilost.dto.ChatDTO;
import edu.cit.chan.unilost.dto.MessageDTO;
import edu.cit.chan.unilost.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** Get all chat rooms for the authenticated user */
    @GetMapping
    public ResponseEntity<List<ChatDTO>> getMyChats(Authentication auth) {
        return ResponseEntity.ok(chatService.getMyChats(auth.getName()));
    }

    /** Get a specific chat room */
    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDTO> getChatById(@PathVariable String chatId, Authentication auth) {
        return ResponseEntity.ok(chatService.getChatById(chatId, auth.getName()));
    }

    /** Get paginated messages in a chat (newest first) */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<MessageDTO>> getMessages(
            @PathVariable String chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication auth) {
        return ResponseEntity.ok(chatService.getMessages(chatId, auth.getName(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    /** Send a message in a chat */
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable String chatId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(chatService.sendMessage(chatId, auth.getName(), content));
    }

    /** Mark all messages in a chat as read */
    @PutMapping("/{chatId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String chatId, Authentication auth) {
        chatService.markAsRead(chatId, auth.getName());
        return ResponseEntity.ok().build();
    }

    /** Get total unread message count across all chats */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication auth) {
        long count = chatService.getTotalUnreadCount(auth.getName());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}
