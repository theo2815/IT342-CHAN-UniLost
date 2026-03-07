package edu.cit.chan.unilost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schools")
public class SchoolEntity {

    @Id
    private String schoolId;

    private String name;

    private String shortName;

    private String city;

    private String emailDomain;

    private boolean isActive = true;

    private LocalDateTime createdAt;
}
