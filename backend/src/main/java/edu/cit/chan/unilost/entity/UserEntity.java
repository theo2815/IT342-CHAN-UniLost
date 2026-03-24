package edu.cit.chan.unilost.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class UserEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @JsonIgnore
    private String passwordHash;

    private String fullName;

    private String profilePictureUrl;

    // Reference to campuses.id
    @Indexed
    private String universityTag;

    private int karmaScore = 0;

    private Role role = Role.STUDENT;

    // Auth & account management
    private boolean emailVerified = false;

    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;

    @JsonIgnore
    private String passwordResetToken;

    @JsonIgnore
    private LocalDateTime passwordResetExpiry;

    @JsonIgnore
    private int otpAttempts = 0;

    @JsonIgnore
    private LocalDateTime otpLockoutUntil;

    @JsonIgnore
    private boolean otpVerified = false;

    @JsonIgnore
    private String resetToken;

    @JsonIgnore
    private LocalDateTime resetTokenExpiry;
}
