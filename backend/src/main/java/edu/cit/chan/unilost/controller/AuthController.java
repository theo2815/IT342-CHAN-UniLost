package edu.cit.chan.unilost.controller;

import edu.cit.chan.unilost.dto.AuthResponse;
import edu.cit.chan.unilost.dto.LoginRequest;
import edu.cit.chan.unilost.dto.RegisterRequest;
import edu.cit.chan.unilost.dto.UserDTO;
import edu.cit.chan.unilost.service.UserService;
import edu.cit.chan.unilost.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Pattern;

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

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,}$");

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        UserDTO createdUser = userService.createUser(registerRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // TODO: [Phase 3] Add email verification endpoint (POST /api/auth/verify-email)
    // TODO: [Phase 3] Add refresh token endpoint (POST /api/auth/refresh)

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            userService.requestPasswordReset(email.trim().toLowerCase());
            return ResponseEntity.ok(Map.of("message", "Verification code sent to your email."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String otp = body.get("otp");
            if (email == null || otp == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email and OTP are required"));
            }
            String resetToken = userService.verifyResetOtp(email.trim().toLowerCase(), otp.trim());
            return ResponseEntity.ok(Map.of("message", "Code verified successfully.", "resetToken", resetToken));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String newPassword = body.get("newPassword");
            String resetToken = body.get("resetToken");
            if (email == null || newPassword == null || resetToken == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email, new password, and reset token are required"));
            }
            if (newPassword.length() < 8 || !PASSWORD_PATTERN.matcher(newPassword).matches()) {
                return ResponseEntity.badRequest().body(Map.of("error",
                        "Password must be at least 8 characters with an uppercase letter, a number, and a special character"));
            }
            userService.resetPassword(email.trim().toLowerCase(), newPassword, resetToken.trim());
            return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now sign in."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        UserDTO user = userService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        String token = jwtUtils.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        String email = (String) authentication.getPrincipal();
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok((Object) user))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found")));
    }
}
