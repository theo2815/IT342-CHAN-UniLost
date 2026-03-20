package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.dto.CampusDTO;
import edu.cit.chan.unilost.dto.RegisterRequest;
import edu.cit.chan.unilost.dto.UpdateUserRequest;
import edu.cit.chan.unilost.dto.UserDTO;
import edu.cit.chan.unilost.entity.AccountStatus;
import edu.cit.chan.unilost.entity.CampusEntity;
import edu.cit.chan.unilost.entity.Role;
import edu.cit.chan.unilost.entity.UserEntity;
import edu.cit.chan.unilost.exception.AuthenticationException;
import edu.cit.chan.unilost.exception.ForbiddenException;
import edu.cit.chan.unilost.exception.ResourceNotFoundException;
import edu.cit.chan.unilost.repository.CampusRepository;
import edu.cit.chan.unilost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    private final MongoTemplate mongoTemplate;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    public UserDTO createUser(RegisterRequest registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Validate email domain against campus whitelist
        String email = registrationDTO.getEmail();
        String domain = extractEmailDomain(email);
        List<CampusEntity> matchingCampuses = campusRepository.findAllByDomainWhitelist(domain);

        if (matchingCampuses.isEmpty()) {
            throw new IllegalArgumentException(
                    "Email domain '" + domain + "' is not recognized. Please use your university email.");
        }

        CampusEntity campus;
        if (matchingCampuses.size() == 1) {
            // Single campus for this domain — auto-assign
            campus = matchingCampuses.get(0);
        } else {
            // Multiple campuses share this domain — campusId is required
            String campusId = registrationDTO.getCampusId();
            if (campusId == null || campusId.isBlank()) {
                throw new IllegalArgumentException(
                        "Multiple campuses use this email domain. Please select your campus.");
            }
            campus = matchingCampuses.stream()
                    .filter(c -> c.getId().equals(campusId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid campus selection for domain '" + domain + "'."));
        }

        UserEntity user = new UserEntity();
        user.setEmail(registrationDTO.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setFullName(registrationDTO.getFullName());
        user.setUniversityTag(campus.getId());
        user.setRole(Role.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        UserEntity saved = userRepository.save(user);
        return convertToDTO(saved);
    }

    public UserDTO authenticate(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new ForbiddenException("Your account has been suspended. Contact your campus admin.");
        }

        if (user.getAccountStatus() == AccountStatus.DEACTIVATED) {
            throw new ForbiddenException("This account has been deactivated.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Update last login timestamp and clear any pending password reset
        user.setLastLogin(LocalDateTime.now());
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setOtpAttempts(0);
        user.setOtpLockoutUntil(null);
        user.setOtpVerified(false);
        userRepository.save(user);

        return convertToDTO(user);
    }

    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public Optional<UserDTO> getUserById(String id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    public Optional<UserDTO> updateUser(String id, UpdateUserRequest updateDTO) {
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
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Silently return to prevent user enumeration
            return;
        }
        UserEntity user = userOpt.get();

        String otp = generateOtp();
        user.setPasswordResetToken(passwordEncoder.encode(otp));
        user.setPasswordResetExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        userRepository.save(user);

        emailService.sendPasswordResetOtp(email, otp);
    }

    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int OTP_LOCKOUT_MINUTES = 15;

    public void verifyResetOtp(String email, String otp) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email address."));

        if (user.getPasswordResetToken() == null || user.getPasswordResetExpiry() == null) {
            throw new IllegalArgumentException("No password reset was requested. Please request a new code.");
        }

        // Check lockout
        if (user.getOtpLockoutUntil() != null && LocalDateTime.now().isBefore(user.getOtpLockoutUntil())) {
            throw new IllegalArgumentException("Too many failed attempts. Please try again later.");
        }

        if (LocalDateTime.now().isAfter(user.getPasswordResetExpiry())) {
            user.setPasswordResetToken(null);
            user.setPasswordResetExpiry(null);
            user.setOtpAttempts(0);
            userRepository.save(user);
            throw new IllegalArgumentException("This code has expired. Please request a new one.");
        }

        if (!passwordEncoder.matches(otp, user.getPasswordResetToken())) {
            // Atomically increment OTP attempts to prevent race conditions
            Update update = new Update().inc("otpAttempts", 1);
            UserEntity updated = mongoTemplate.findAndModify(
                    Query.query(Criteria.where("id").is(user.getId())),
                    update,
                    FindAndModifyOptions.options().returnNew(true),
                    UserEntity.class
            );
            if (updated != null && updated.getOtpAttempts() >= MAX_OTP_ATTEMPTS) {
                mongoTemplate.updateFirst(
                        Query.query(Criteria.where("id").is(user.getId())),
                        new Update()
                                .set("otpLockoutUntil", LocalDateTime.now().plusMinutes(OTP_LOCKOUT_MINUTES))
                                .set("otpAttempts", 0),
                        UserEntity.class
                );
            }
            throw new AuthenticationException("Invalid verification code. Please try again.");
        }

        // Success: invalidate OTP immediately and mark as verified
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setOtpAttempts(0);
        user.setOtpLockoutUntil(null);
        user.setOtpVerified(true);
        userRepository.save(user);
    }

    public void resetPassword(String email, String newPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email address."));

        if (!user.isOtpVerified()) {
            throw new IllegalArgumentException("OTP has not been verified. Please verify your code first.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setOtpVerified(false);
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
            throw new IllegalArgumentException("Invalid email format");
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
        dto.setRole(user.getRole().name());
        dto.setAccountStatus(user.getAccountStatus().name());
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
