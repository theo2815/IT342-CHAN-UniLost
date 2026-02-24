package com.hulampay.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class UserEntity {

    @Id
    private String userId;

    @DBRef
    private SchoolEntity school;

    private String firstName;

    private String lastName;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String address;

    private String phoneNumber;

    private String profilePicture;

    @Indexed(unique = true)
    private String studentIdNumber;

    // Role: STUDENT, ADMIN, SUPER_ADMIN
    private String role = "STUDENT";

    // Karma & stats (used in Phase 5, initialized here)
    private int karmaScore = 0;
    private int totalItemsReturned = 0;
    private int totalItemsClaimed = 0;

    // Account status
    private boolean isVerified = false;
    private boolean isBanned = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
