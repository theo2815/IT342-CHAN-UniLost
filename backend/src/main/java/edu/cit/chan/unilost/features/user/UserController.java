package edu.cit.chan.unilost.features.user;

import edu.cit.chan.unilost.features.auth.ChangePasswordRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        Page<UserDTO> users = userService.getAllUsers(pageable);

        // Strip sensitive fields for non-admin callers
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            String currentEmail = auth != null ? (String) auth.getPrincipal() : null;
            users.getContent().forEach(user -> {
                if (user.getEmail() == null || !user.getEmail().equals(currentEmail)) {
                    user.setEmail(null);
                    user.setAccountStatus(null);
                }
            });
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(user -> {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    String currentEmail = auth != null ? (String) auth.getPrincipal() : null;
                    boolean isSelf = user.getEmail() != null && user.getEmail().equals(currentEmail);
                    boolean isAdmin = auth != null && auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    if (!isSelf && !isAdmin) {
                        user.setEmail(null);
                        user.setAccountStatus(null);
                    }
                    return ResponseEntity.ok(user);
                })
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
                                .body((Object) Map.of("error", "You can only update your own profile"));
                    }
                    try {
                        return userService.updateUser(id, updateDTO)
                                .map(updated -> ResponseEntity.ok((Object) updated))
                                .orElse(ResponseEntity.notFound().build());
                    } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body((Object) Map.of("error", e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@PathVariable String id,
                                                   @RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = (String) auth.getPrincipal();

        return userService.getUserById(id)
                .map(existingUser -> {
                    if (!existingUser.getEmail().equals(currentEmail)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body((Object) Map.of("error", "You can only update your own profile"));
                    }
                    try {
                        UserDTO updated = userService.updateProfilePicture(id, file);
                        return ResponseEntity.ok((Object) updated);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body((Object) Map.of("error", e.getMessage()));
                    } catch (IOException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body((Object) Map.of("error", "Failed to upload image"));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable String id,
                                             @Valid @RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = (String) auth.getPrincipal();

        return userService.getUserById(id)
                .map(existingUser -> {
                    if (!existingUser.getEmail().equals(currentEmail)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body((Object) Map.of("error", "You can only change your own password"));
                    }
                    try {
                        userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());
                        return ResponseEntity.ok((Object) Map.of("message", "Password changed successfully"));
                    } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body((Object) Map.of("error", e.getMessage()));
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
                                .body((Object) Map.of("error", "You can only delete your own account"));
                    }
                    if (userService.deleteUser(id)) {
                        return ResponseEntity.noContent().build();
                    }
                    return ResponseEntity.notFound().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
