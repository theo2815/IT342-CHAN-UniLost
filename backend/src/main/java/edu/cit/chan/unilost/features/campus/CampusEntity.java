package edu.cit.chan.unilost.features.campus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "campuses")
public class CampusEntity {

    @Id
    private String id;

    private String universityCode;  // e.g. "USC", "CIT-U"

    private String campusName;      // e.g. "Talamban Campus", "Main Campus"

    private String name;            // full display name e.g. "University of San Carlos - Talamban Campus"

    private String shortLabel;      // short UI label e.g. "USC Talamban", "CIT-U"

    private String address;         // physical street address

    private String domainWhitelist; // email domain e.g. "usc.edu.ph"

    private GeoJsonPoint centerCoordinates;
}
