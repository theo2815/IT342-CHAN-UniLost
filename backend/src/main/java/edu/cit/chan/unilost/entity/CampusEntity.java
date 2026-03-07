package edu.cit.chan.unilost.entity;

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

    private String name;

    private String domainWhitelist;

    private GeoJsonPoint centerCoordinates;
}
