package edu.cit.chan.unilost.controller;

import edu.cit.chan.unilost.dto.NotificationDTO;
import edu.cit.chan.unilost.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = getEmail();
        return ResponseEntity.ok(notificationService.getNotifications(email,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        String email = getEmail();
        long count = notificationService.getUnreadCount(email);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable String id) {
        String email = getEmail();
        return ResponseEntity.ok(notificationService.markAsRead(id, email));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        String email = getEmail();
        notificationService.markAllAsRead(email);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        String email = getEmail();
        notificationService.deleteNotification(id, email);
        return ResponseEntity.noContent().build();
    }

    private String getEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
    }
}
