package edu.cit.chan.unilost.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be 100 characters or less")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be 1000 characters or less")
    private String description;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "LOST|FOUND", message = "Type must be LOST or FOUND")
    private String type;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "ELECTRONICS|WALLETS|CLOTHING|DOCUMENTS|ACCESSORIES|BOOKS|KEYS|BAGS|OTHER",
            message = "Invalid category")
    private String category;

    @Size(max = 200, message = "Location must be 200 characters or less")
    private String location;

    @Size(max = 500, message = "Secret detail question must be 500 characters or less")
    private String secretDetailQuestion;

    private String dateLostFound;

    private String campusId;
}
