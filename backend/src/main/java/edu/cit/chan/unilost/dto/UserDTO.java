package edu.cit.chan.unilost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String userId;
    private String schoolId;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phoneNumber;
    private String profilePicture;
    private String studentIdNumber;
    private String role;
    private int karmaScore;
    private boolean isVerified;
    private boolean isBanned;
    private LocalDateTime createdAt;

    // School details (for response)
    private SchoolDTO school;
}
