package edu.cit.chan.unilost.controller;

import edu.cit.chan.unilost.dto.ItemDTO;
import edu.cit.chan.unilost.dto.ItemRequest;
import edu.cit.chan.unilost.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemDTO> createItem(
            @Valid @RequestPart("item") ItemRequest itemRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) throws IOException {

        String email = (String) authentication.getPrincipal();
        ItemDTO created = itemService.createItem(itemRequest, email, images);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ItemDTO>> getItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String campusId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        size = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ItemDTO> items = itemService.searchItems(keyword, campusId, category, type, status, pageable);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable String id, Authentication authentication) {
        String currentEmail = resolveEmail(authentication);
        return itemService.getItemById(id, currentEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemDTO> updateItem(
            @PathVariable String id,
            @Valid @RequestPart("item") ItemRequest itemRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) throws IOException {

        String email = (String) authentication.getPrincipal();
        return itemService.updateItem(id, itemRequest, email, images)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id, Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        if (itemService.softDeleteItem(id, email)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ItemDTO>> getItemsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        size = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(itemService.getItemsByUser(userId, pageable));
    }

    @GetMapping("/campus/{campusId}")
    public ResponseEntity<Page<ItemDTO>> getItemsByCampus(
            @PathVariable String campusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        size = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(itemService.getItemsByCampus(campusId, pageable));
    }

    /**
     * Extract authenticated email, returning null for anonymous/unauthenticated requests.
     */
    private String resolveEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof String email && !"anonymousUser".equals(email)) {
            return email;
        }
        return null;
    }
}
