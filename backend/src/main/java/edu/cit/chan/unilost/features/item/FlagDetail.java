package edu.cit.chan.unilost.features.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlagDetail {

    private String reporterId;

    private String reason;

    private String description;

    private LocalDateTime createdAt;
}
