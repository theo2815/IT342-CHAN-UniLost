package edu.cit.chan.unilost.controller;

import edu.cit.chan.unilost.dto.UserDTO;
import edu.cit.chan.unilost.dto.UserRegistrationDTO;
import edu.cit.chan.unilost.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserRegistrationDTO updateDTO) {
        // Users can only update their own profile
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
        // Users can only delete their own account
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
