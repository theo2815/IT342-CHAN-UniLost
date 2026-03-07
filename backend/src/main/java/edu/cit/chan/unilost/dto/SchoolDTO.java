package edu.cit.chan.unilost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDTO {

    private String schoolId;
    private String name;
    private String shortName;
    private String city;
    private String emailDomain;
}
