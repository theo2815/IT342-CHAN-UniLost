package edu.cit.chan.unilost.controller;

import edu.cit.chan.unilost.dto.UpdateUserRequest;
import edu.cit.chan.unilost.dto.UserDTO;
import edu.cit.chan.unilost.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User profile management endpoints.
 *
 * Phase 3 — Authentication System (user CRUD)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Public leaderboard — top users ranked by karma score. */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserDTO>> getLeaderboard(
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String campusId) {
        return ResponseEntity.ok(userService.getLeaderboard(size, campusId));
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserRequest updateDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = (String) auth.getPrincipal();

        return userService.getUserById(id)
                .map(existingUser -> {
                    if (!existingUser.getEmail().equals(currentEmail)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body((Object) "You can only update your own profile");
                    }
                    try {
                        return userService.updateUser(id, updateDTO)
                                .map(updated -> ResponseEntity.ok((Object) updated))
                                .orElse(ResponseEntity.notFound().build());
                    } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body((Object) e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = (String) auth.getPrincipal();

        return userService.getUserById(id)
                .map(existingUser -> {
                    if (!existingUser.getEmail().equals(currentEmail)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body((Object) "You can only delete your own account");
                    }
                    if (userService.deleteUser(id)) {
                        return ResponseEntity.noContent().build();
                    }
                    return ResponseEntity.notFound().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
