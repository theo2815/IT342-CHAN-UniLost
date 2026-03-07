package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.dto.CampusDTO;
import edu.cit.chan.unilost.dto.RegisterRequest;
import edu.cit.chan.unilost.dto.UserDTO;
import edu.cit.chan.unilost.entity.CampusEntity;
import edu.cit.chan.unilost.entity.UserEntity;
import edu.cit.chan.unilost.repository.CampusRepository;
import edu.cit.chan.unilost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core user management service.
 *
 * Phase 3 — Authentication System
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CampusRepository campusRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    public UserDTO createUser(RegisterRequest registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Validate email domain against campus whitelist
        String email = registrationDTO.getEmail();
        String domain = extractEmailDomain(email);
        CampusEntity campus = campusRepository.findByDomainWhitelist(domain)
                .orElseThrow(() -> new RuntimeException(
                        "Email domain '" + domain + "' is not recognized. Please use your university email."));

        UserEntity user = new UserEntity();
        user.setEmail(registrationDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setFullName(registrationDTO.getFullName());
        user.setUniversityTag(campus.getId());
        user.setRole("STUDENT");
        user.setAccountStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        UserEntity saved = userRepository.save(user);
        return convertToDTO(saved);
    }

    public UserDTO authenticate(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if ("SUSPENDED".equals(user.getAccountStatus())) {
            throw new RuntimeException("Your account has been suspended. Contact your campus admin.");
        }

        if ("DEACTIVATED".equals(user.getAccountStatus())) {
            throw new RuntimeException("This account has been deactivated.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserDTO> getUserById(String id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    public Optional<UserDTO> updateUser(String id, RegisterRequest updateDTO) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (updateDTO.getFullName() != null) {
                        existingUser.setFullName(updateDTO.getFullName());
                    }
                    if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
                        existingUser.setPasswordHash(passwordEncoder.encode(updateDTO.getPassword()));
                    }
                    return convertToDTO(userRepository.save(existingUser));
                });
    }

    public boolean deleteUser(String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ── Password Reset Flow ────────────────────────────────────

    public void requestPasswordReset(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with that email address."));

        String otp = generateOtp();
        user.setPasswordResetToken(passwordEncoder.encode(otp));
        user.setPasswordResetExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        userRepository.save(user);

        emailService.sendPasswordResetOtp(email, otp);
    }

    public void verifyResetOtp(String email, String otp) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with that email address."));

        if (user.getPasswordResetToken() == null || user.getPasswordResetExpiry() == null) {
            throw new RuntimeException("No password reset was requested. Please request a new code.");
        }

        if (LocalDateTime.now().isAfter(user.getPasswordResetExpiry())) {
            user.setPasswordResetToken(null);
            user.setPasswordResetExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("This code has expired. Please request a new one.");
        }

        if (!passwordEncoder.matches(otp, user.getPasswordResetToken())) {
            throw new RuntimeException("Invalid verification code. Please try again.");
        }
    }

    public void resetPassword(String email, String otp, String newPassword) {
        // Re-verify OTP before changing password
        verifyResetOtp(email, otp);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with that email address."));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int otp = random.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }

    private String extractEmailDomain(String email) {
        if (email == null || !email.contains("@")) {
            throw new RuntimeException("Invalid email format");
        }
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }

    private UserDTO convertToDTO(UserEntity user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setUniversityTag(user.getUniversityTag());
        dto.setKarmaScore(user.getKarmaScore());
        dto.setRole(user.getRole());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setCreatedAt(user.getCreatedAt());

        // Resolve campus details
        if (user.getUniversityTag() != null) {
            campusRepository.findById(user.getUniversityTag())
                    .ifPresent(campus -> {
                        CampusDTO campusDTO = new CampusDTO();
                        campusDTO.setId(campus.getId());
                        campusDTO.setName(campus.getName());
                        campusDTO.setDomainWhitelist(campus.getDomainWhitelist());
                        if (campus.getCenterCoordinates() != null) {
                            campusDTO.setCenterCoordinates(new double[]{
                                    campus.getCenterCoordinates().getX(),
                                    campus.getCenterCoordinates().getY()
                            });
                        }
                        dto.setCampus(campusDTO);
                    });
        }

        return dto;
    }
}
