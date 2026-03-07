package edu.cit.chan.unilost.controller;

import edu.cit.chan.unilost.dto.AuthResponse;
import edu.cit.chan.unilost.dto.LoginRequest;
import edu.cit.chan.unilost.dto.RegisterRequest;
import edu.cit.chan.unilost.dto.UserDTO;
import edu.cit.chan.unilost.service.UserService;
import edu.cit.chan.unilost.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles user authentication: registration, login, and session queries.
 *
 * Phase 3 — Authentication System
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            UserDTO createdUser = userService.createUser(registerRequest);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // TODO: [Phase 3] Add email verification endpoint (POST /api/auth/verify-email)
    // TODO: [Phase 3] Add refresh token endpoint (POST /api/auth/refresh)

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            userService.requestPasswordReset(email.trim().toLowerCase());
            return ResponseEntity.ok(Map.of("message", "Verification code sent to your email."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String otp = body.get("otp");
            if (email == null || otp == null) {
                return ResponseEntity.badRequest().body("Email and OTP are required");
            }
            userService.verifyResetOtp(email.trim().toLowerCase(), otp.trim());
            return ResponseEntity.ok(Map.of("message", "Code verified successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String otp = body.get("otp");
            String newPassword = body.get("newPassword");
            if (email == null || otp == null || newPassword == null) {
                return ResponseEntity.badRequest().body("Email, OTP, and new password are required");
            }
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body("Password must be at least 6 characters");
            }
            userService.resetPassword(email.trim().toLowerCase(), otp.trim(), newPassword);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now sign in."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            UserDTO user = userService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
            String token = jwtUtils.generateToken(user.getEmail(), user.getRole());
            return ResponseEntity.ok(new AuthResponse(token, user));
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

        String email = (String) authentication.getPrincipal();
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok((Object) user))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }
}
