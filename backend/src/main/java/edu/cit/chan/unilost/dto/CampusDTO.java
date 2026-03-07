package edu.cit.chan.unilost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusDTO {

    private String id;
    private String name;
    private String domainWhitelist;
    private double[] centerCoordinates; // [lng, lat]
}
