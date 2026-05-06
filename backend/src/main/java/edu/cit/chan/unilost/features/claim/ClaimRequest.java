package edu.cit.chan.unilost.features.claim;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRequest {

    @NotBlank(message = "Item ID is required")
    private String itemId;

    @Size(max = 500, message = "Answer must be 500 characters or less")
    private String providedAnswer;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message must be 500 characters or less")
    private String message;
}
