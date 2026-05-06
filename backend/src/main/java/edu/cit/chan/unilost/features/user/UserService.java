package edu.cit.chan.unilost.features.user;

import edu.cit.chan.unilost.features.auth.RegisterRequest;
import edu.cit.chan.unilost.features.campus.CampusEntity;
import edu.cit.chan.unilost.features.campus.CampusRepository;
import edu.cit.chan.unilost.exception.AuthenticationException;
import edu.cit.chan.unilost.exception.ForbiddenException;
import edu.cit.chan.unilost.exception.ResourceNotFoundException;
import edu.cit.chan.unilost.service.CloudinaryService;
import edu.cit.chan.unilost.service.EmailService;
import edu.cit.chan.unilost.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
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
    private final CloudinaryService cloudinaryService;

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
        Page<UserEntity> userPage = userRepository.findAll(pageable);
        // Batch-load campuses to prevent N+1 queries
        Set<String> campusIds = userPage.getContent().stream()
                .map(UserEntity::getUniversityTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, CampusEntity> campusMap = campusRepository.findAllById(campusIds).stream()
                .collect(Collectors.toMap(CampusEntity::getId, c -> c));
        return userPage.map(user -> convertToDTO(user, campusMap));
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
                    return convertToDTO(userRepository.save(existingUser));
                });
    }

    private static final long MAX_PROFILE_PICTURE_SIZE = 5 * 1024 * 1024; // 5 MB

    public UserDTO updateProfilePicture(String userId, MultipartFile file) throws IOException {
        if (file.getSize() > MAX_PROFILE_PICTURE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 5 MB");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Delete old profile picture from Cloudinary if present
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            cloudinaryService.deleteImage(user.getProfilePictureUrl());
        }

        String url = cloudinaryService.uploadImage(file);
        user.setProfilePictureUrl(url);
        return convertToDTO(userRepository.save(user));
    }

    public void changePassword(String userId, String currentPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean deleteUser(String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Public leaderboard: top users by karma score.
     * Returns only active users, limited to the requested size.
     * Optionally filtered by campus. Email is stripped for privacy.
     */
    public List<UserDTO> getLeaderboard(int size, String campusId) {
        Pageable pageable = PageRequest.of(0, Math.min(Math.max(size, 1), 50));
        List<UserEntity> topUsers;
        if (campusId != null && !campusId.isEmpty()) {
            topUsers = userRepository.findByUniversityTagAndAccountStatusAndKarmaScoreGreaterThanOrderByKarmaScoreDesc(
                    campusId, AccountStatus.ACTIVE, 0, pageable);
        } else {
            topUsers = userRepository.findByAccountStatusAndKarmaScoreGreaterThanOrderByKarmaScoreDesc(
                    AccountStatus.ACTIVE, 0, pageable);
        }
        // Batch-load campuses to resolve campus names (single query)
        Set<String> campusIds = topUsers.stream()
                .map(UserEntity::getUniversityTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, CampusEntity> campusMap = campusRepository.findAllById(campusIds).stream()
                .collect(Collectors.toMap(CampusEntity::getId, c -> c));
        return topUsers.stream().map(user -> {
            UserDTO dto = DtoMapper.toUserSummaryDTO(user);
            dto.setEmail(null); // strip email for public endpoint
            dto.setAccountStatus(null);
            dto.setRole(null);
            CampusEntity campus = campusMap.get(user.getUniversityTag());
            if (campus != null) {
                dto.setCampus(DtoMapper.toCampusPreviewDTO(campus));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // ── Password Reset Flow ────────────────────────────────────

    public void requestPasswordReset(String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Add random delay to prevent timing-based user enumeration
            try {
                Thread.sleep(200 + new SecureRandom().nextInt(301)); // 200-500ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
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

    private static final int RESET_TOKEN_EXPIRY_MINUTES = 10;

    public String verifyResetOtp(String email, String otp) {
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

        // Success: invalidate OTP, generate a single-use reset token bound to this session
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setOtpAttempts(0);
        user.setOtpLockoutUntil(null);
        user.setOtpVerified(true);
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
        userRepository.save(user);

        return resetToken;
    }

    public void resetPassword(String email, String newPassword, String resetToken) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email address."));

        if (user.getResetToken() == null || !user.getResetToken().equals(resetToken)) {
            throw new IllegalArgumentException("Invalid or expired reset token. Please verify your code again.");
        }

        if (user.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            user.setOtpVerified(false);
            userRepository.save(user);
            throw new IllegalArgumentException("Reset token has expired. Please verify your code again.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setOtpVerified(false);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    /**
     * Atomically increment a user's karma score.
     * Uses MongoTemplate findAndModify to prevent race conditions.
     */
    public void incrementKarma(String userId, int points) {
        mongoTemplate.findAndModify(
                Query.query(Criteria.where("id").is(userId)),
                new Update().inc("karmaScore", points),
                UserEntity.class
        );
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
        return convertToDTO(user, null);
    }

    private UserDTO convertToDTO(UserEntity user, Map<String, CampusEntity> campusMap) {
        UserDTO dto = DtoMapper.toUserSummaryDTO(user);

        // Resolve campus details
        if (user.getUniversityTag() != null) {
            CampusEntity campus = null;
            if (campusMap != null) {
                campus = campusMap.get(user.getUniversityTag());
            } else {
                campus = campusRepository.findById(user.getUniversityTag()).orElse(null);
            }
            if (campus != null) {
                dto.setCampus(DtoMapper.toCampusPreviewDTO(campus));
            }
        }

        return dto;
    }
}
