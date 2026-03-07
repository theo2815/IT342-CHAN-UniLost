package edu.cit.chan.unilost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private UserDTO user;

    public JwtResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
}
