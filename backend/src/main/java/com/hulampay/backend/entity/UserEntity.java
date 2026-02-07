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
    
    private LocalDateTime createdAt;
}
