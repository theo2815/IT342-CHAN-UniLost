package edu.cit.chan.unilost.features.user;

import edu.cit.chan.unilost.features.auth.RegisterRequest;
import edu.cit.chan.unilost.features.campus.CampusEntity;
import edu.cit.chan.unilost.features.campus.CampusRepository;
import edu.cit.chan.unilost.features.cloudinary.CloudinaryService;
import edu.cit.chan.unilost.features.email.EmailService;
import edu.cit.chan.unilost.shared.exception.AuthenticationException;
import edu.cit.chan.unilost.shared.exception.ForbiddenException;
import edu.cit.chan.unilost.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserService.
 *
 * Covers: registration (campus domain matching), authentication (status guards,
 * password verification), password reset OTP flow (request, verify, reset),
 * and karma increment.
 *
 * Pure JUnit 5 + Mockito — no Spring context, no MongoDB.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CampusRepository campusRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private MongoTemplate mongoTemplate;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks private UserService userService;

    private CampusEntity uscCampus;
    private UserEntity activeUser;

    @BeforeEach
    void setUp() {
        uscCampus = new CampusEntity();
        uscCampus.setId("campus-usc");
        uscCampus.setName("University of San Carlos");
        uscCampus.setDomainWhitelist("usc.edu.ph");

        activeUser = new UserEntity();
        activeUser.setId("user-1");
        activeUser.setEmail("alice@usc.edu.ph");
        activeUser.setPasswordHash("hashed-pw");
        activeUser.setFullName("Alice Cruz");
        activeUser.setUniversityTag("campus-usc");
        activeUser.setRole(Role.STUDENT);
        activeUser.setAccountStatus(AccountStatus.ACTIVE);
        activeUser.setCreatedAt(LocalDateTime.now());
    }

    // ── createUser ──────────────────────────────────────────

    @Test
    @DisplayName("createUser auto-assigns campus when only one matches the email domain")
    void createUser_autoAssignsCampus_whenSingleDomainMatch() {
        RegisterRequest req = new RegisterRequest("Alice Cruz", "alice@usc.edu.ph", "Pass1234!", null);

        when(userRepository.existsByEmail("alice@usc.edu.ph")).thenReturn(false);
        when(campusRepository.findAllByDomainWhitelist("usc.edu.ph")).thenReturn(List.of(uscCampus));
        when(passwordEncoder.encode("Pass1234!")).thenReturn("hashed-pw");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            u.setId("user-1");
            return u;
        });

        UserDTO dto = userService.createUser(req);

        assertThat(dto.getEmail()).isEqualTo("alice@usc.edu.ph");
        assertThat(dto.getUniversityTag()).isEqualTo("campus-usc");
        assertThat(dto.getRole()).isEqualTo("STUDENT");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-pw");
        assertThat(saved.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    @DisplayName("createUser rejects duplicate email")
    void createUser_rejectsDuplicate() {
        RegisterRequest req = new RegisterRequest("Alice", "alice@usc.edu.ph", "Pass1234!", null);
        when(userRepository.existsByEmail("alice@usc.edu.ph")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser rejects unknown email domain")
    void createUser_rejectsUnknownDomain() {
        RegisterRequest req = new RegisterRequest("Alice", "alice@nope.com", "Pass1234!", null);
        when(userRepository.existsByEmail("alice@nope.com")).thenReturn(false);
        when(campusRepository.findAllByDomainWhitelist("nope.com")).thenReturn(List.of());

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not recognized");
    }

    @Test
    @DisplayName("createUser requires campusId when domain maps to multiple campuses")
    void createUser_requiresCampusId_whenMultipleMatch() {
        CampusEntity citu = new CampusEntity();
        citu.setId("campus-citu");
        citu.setDomainWhitelist("shared.edu.ph");
        CampusEntity usc = new CampusEntity();
        usc.setId("campus-usc-2");
        usc.setDomainWhitelist("shared.edu.ph");

        RegisterRequest req = new RegisterRequest("Alice", "alice@shared.edu.ph", "Pass1234!", null);
        when(userRepository.existsByEmail("alice@shared.edu.ph")).thenReturn(false);
        when(campusRepository.findAllByDomainWhitelist("shared.edu.ph")).thenReturn(List.of(citu, usc));

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("select your campus");
    }

    // ── authenticate ────────────────────────────────────────

    @Test
    @DisplayName("authenticate returns DTO and updates lastLogin on success")
    void authenticate_success() {
        when(userRepository.findByEmail("alice@usc.edu.ph")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Pass1234!", "hashed-pw")).thenReturn(true);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDTO dto = userService.authenticate("alice@usc.edu.ph", "Pass1234!");

        assertThat(dto.getEmail()).isEqualTo("alice@usc.edu.ph");
        assertThat(activeUser.getLastLogin()).isNotNull();
        verify(userRepository).save(activeUser);
    }

    @Test
    @DisplayName("authenticate fails for unknown email")
    void authenticate_unknownEmail() {
        when(userRepository.findByEmail("ghost@usc.edu.ph")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.authenticate("ghost@usc.edu.ph", "x"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("authenticate fails for wrong password")
    void authenticate_wrongPassword() {
        when(userRepository.findByEmail("alice@usc.edu.ph")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", "hashed-pw")).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticate("alice@usc.edu.ph", "wrong"))
                .isInstanceOf(AuthenticationException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("authenticate blocks suspended account")
    void authenticate_blocksSuspended() {
        activeUser.setAccountStatus(AccountStatus.SUSPENDED);
        when(userRepository.findByEmail("alice@usc.edu.ph")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> userService.authenticate("alice@usc.edu.ph", "any"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("suspended");
    }

    @Test
    @DisplayName("authenticate blocks deactivated account")
    void authenticate_blocksDeactivated() {
        activeUser.setAccountStatus(AccountStatus.DEACTIVATED);
        when(userRepository.findByEmail("alice@usc.edu.ph")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> userService.authenticate("alice@usc.edu.ph", "any"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("deactivated");
    }

    // ── password reset OTP ──────────────────────────────────

    @Test
    @DisplayName("requestPasswordReset stores hashed OTP and sends email")
    void requestPasswordReset_sendsOtp() {
        when(userRepository.findByEmail("alice@usc.edu.ph")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-otp");

        userService.requestPasswordReset("alice@usc.edu.ph");

        assertThat(activeUser.getPasswordResetToken()).isEqualTo("hashed-otp");
        assertThat(activeUser.getPasswordResetExpiry()).isAfter(LocalDateTime.now());
        verify(userRepository).save(activeUser);
        verify(emailService).sendPasswordResetOtp(eq("alice@usc.edu.ph"), anyString());
    }

    @Test
    @DisplayName("requestPasswordReset is silent for unknown email (no enumeration)")
    void requestPasswordReset_silentForUnknown() {
        when(userRepository.findByEmail("ghost@usc.edu.ph")).thenReturn(Optional.empty());

        userService.requestPasswordReset("ghost@usc.edu.ph");

        verify(emailService, never()).sendPasswordResetOtp(anyString(), anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("verifyResetOtp throws when no reset was requested")
    void verifyResetOtp_throwsWhenNotRequested() {
        activeUser.setPasswordResetToken(null);
        when(userRepository.findByEmail("alice@usc.edu.ph")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> userService.verifyResetOtp("alice@usc.edu.ph", "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No password reset");
    }

    @Test
    @DisplayName("verifyResetOtp throws when expired and clears token")
    void verifyResetOtp_throwsWhenExpired() {
        activeUser.setPasswordResetToken("hashed-otp");
        activeUser.setPasswordResetExpiry(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail("alice@usc.edu.ph")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> userService.verifyResetOtp("alice@usc.edu.ph", "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired");
        assertThat(activeUser.getPasswordResetToken()).isNull();
        verify(userRepository).save(activeUser);
    }

    @Test
    @DisplayName("verifyResetOtp returns reset token on success")
    void verifyResetOtp_success() {
        activeUser.setPasswordResetToken("hashed-otp");
        activeUser.setPasswordResetExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail("alice@usc.edu.ph")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("123456", "hashed-otp")).thenReturn(true);

        String resetToken = userService.verifyResetOtp("alice@usc.edu.ph", "123456");

        assertThat(resetToken).isNotBlank();
        assertThat(activeUser.isOtpVerified()).isTrue();
        assertThat(activeUser.getResetToken()).isEqualTo(resetToken);
        assertThat(activeUser.getPasswordResetToken()).isNull();
    }

    @Test
    @DisplayName("resetPassword updates password hash on valid token")
    void resetPassword_success() {
        String token = "valid-reset-token";
        activeUser.setResetToken(token);
        activeUser.setResetTokenExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail("alice@usc.edu.ph")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.encode("NewPass1234!")).thenReturn("new-hash");

        userService.resetPassword("alice@usc.edu.ph", "NewPass1234!", token);

        assertThat(activeUser.getPasswordHash()).isEqualTo("new-hash");
        assertThat(activeUser.getResetToken()).isNull();
        assertThat(activeUser.isOtpVerified()).isFalse();
        verify(userRepository).save(activeUser);
    }

    @Test
    @DisplayName("resetPassword rejects invalid token")
    void resetPassword_rejectsInvalidToken() {
        activeUser.setResetToken("real-token");
        activeUser.setResetTokenExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail("alice@usc.edu.ph")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> userService.resetPassword("alice@usc.edu.ph", "NewPass1234!", "wrong-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired reset token");
    }

    @Test
    @DisplayName("resetPassword rejects expired token and clears it")
    void resetPassword_rejectsExpired() {
        String token = "valid-but-expired";
        activeUser.setResetToken(token);
        activeUser.setResetTokenExpiry(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail("alice@usc.edu.ph")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> userService.resetPassword("alice@usc.edu.ph", "NewPass1234!", token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired");
        assertThat(activeUser.getResetToken()).isNull();
    }

    // ── changePassword ──────────────────────────────────────

    @Test
    @DisplayName("changePassword rejects when current password is wrong")
    void changePassword_rejectsWrongCurrent() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", "hashed-pw")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("user-1", "wrong", "NewPass1234!"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    @Test
    @DisplayName("changePassword updates hash when current password matches")
    void changePassword_success() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Pass1234!", "hashed-pw")).thenReturn(true);
        when(passwordEncoder.encode("NewPass1234!")).thenReturn("new-hash");

        userService.changePassword("user-1", "Pass1234!", "NewPass1234!");

        assertThat(activeUser.getPasswordHash()).isEqualTo("new-hash");
        verify(userRepository).save(activeUser);
    }

    @Test
    @DisplayName("changePassword throws ResourceNotFound for missing user")
    void changePassword_missingUser() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.changePassword("missing", "x", "Y"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── incrementKarma (atomic) ─────────────────────────────

    @Test
    @DisplayName("incrementKarma issues a Mongo findAndModify with $inc")
    void incrementKarma_issuesAtomicUpdate() {
        userService.incrementKarma("user-1", 10);
        verify(mongoTemplate, times(1)).findAndModify(any(), any(), eq(UserEntity.class));
    }
}
