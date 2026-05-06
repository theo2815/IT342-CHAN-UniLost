package edu.cit.chan.unilost.features.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkActionRequest {
    private List<String> ids;
    private String status;
}
