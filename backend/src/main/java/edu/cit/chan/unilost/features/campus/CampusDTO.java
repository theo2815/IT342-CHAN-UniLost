package edu.cit.chan.unilost.features.campus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusDTO {

    private String id;
    private String universityCode;
    private String campusName;
    private String name;
    private String shortLabel;
    private String address;
    private String domainWhitelist;
    private double[] centerCoordinates; // [lng, lat]
}
