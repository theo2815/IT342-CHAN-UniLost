package edu.cit.chan.unilost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String id;
    private String email;
    private String fullName;
    private String universityTag;
    private int karmaScore;
    private String role;
    private boolean emailVerified;
    private String accountStatus;
    private LocalDateTime createdAt;

    // Resolved campus details (for API responses)
    private CampusDTO campus;
}
