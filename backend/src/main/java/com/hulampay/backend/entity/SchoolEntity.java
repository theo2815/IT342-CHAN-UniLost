package com.hulampay.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schools")
public class SchoolEntity {
    
    @Id
    private String schoolId;
    
    private String name;
    
    private String city;
    
    private String emailDomain;
}
