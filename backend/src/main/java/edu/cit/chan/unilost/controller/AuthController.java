package edu.cit.chan.unilost.controller;

import edu.cit.chan.unilost.dto.JwtResponse;
import edu.cit.chan.unilost.dto.LoginRequest;
import edu.cit.chan.unilost.dto.UserDTO;
import edu.cit.chan.unilost.dto.UserRegistrationDTO;
import edu.cit.chan.unilost.service.UserService;
import edu.cit.chan.unilost.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        try {
            UserDTO createdUser = userService.createUser(registrationDTO);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            UserDTO user = userService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
            String token = jwtUtils.generateToken(user.getEmail(), user.getRole());
            return ResponseEntity.ok(new JwtResponse(token, user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        // The principal is the user's email (set by JwtAuthenticationFilter)
        String email = (String) authentication.getPrincipal();
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok((Object) user))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }
}
