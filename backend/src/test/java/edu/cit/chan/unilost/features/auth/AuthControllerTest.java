package edu.cit.chan.unilost.features.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.chan.unilost.features.user.UserDTO;
import edu.cit.chan.unilost.features.user.UserService;
import edu.cit.chan.unilost.shared.exception.AuthenticationException;
import edu.cit.chan.unilost.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc test for AuthController.
 *
 * Verifies that after the VSA refactor the auth slice (controller + JwtUtils +
 * GlobalExceptionHandler) is wired correctly: routes resolve, request bodies
 * parse, the global exception handler maps exceptions to HTTP status codes.
 *
 * Uses {@code MockMvcBuilders.standaloneSetup} so we do NOT need to start a
 * full Spring context — keeps the test hermetic and fast and avoids needing
 * MongoDB / Mail credentials at test time.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private UserService userService;
    @Mock private JwtUtils jwtUtils;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(userService, jwtUtils);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // ── /api/auth/register ──────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/register returns 201 with the created UserDTO")
    void register_success() throws Exception {
        RegisterRequest req = new RegisterRequest("Alice Cruz", "alice@usc.edu.ph", "Pass1234!", null);
        UserDTO created = new UserDTO();
        created.setId("user-1");
        created.setEmail("alice@usc.edu.ph");
        created.setFullName("Alice Cruz");
        created.setRole("STUDENT");

        when(userService.createUser(any(RegisterRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("user-1"))
                .andExpect(jsonPath("$.email").value("alice@usc.edu.ph"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("POST /api/auth/register surfaces 400 when service rejects duplicate email")
    void register_duplicateEmail() throws Exception {
        RegisterRequest req = new RegisterRequest("Alice", "alice@usc.edu.ph", "Pass1234!", null);
        when(userService.createUser(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    // ── /api/auth/login ─────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login returns 200 with bearer token on success")
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@usc.edu.ph");
        req.setPassword("Pass1234!");

        UserDTO user = new UserDTO();
        user.setId("user-1");
        user.setEmail("alice@usc.edu.ph");
        user.setRole("STUDENT");

        when(userService.authenticate("alice@usc.edu.ph", "Pass1234!")).thenReturn(user);
        when(jwtUtils.generateToken("alice@usc.edu.ph", "STUDENT")).thenReturn("test-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("alice@usc.edu.ph"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 on bad credentials")
    void login_badCredentials() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@usc.edu.ph");
        req.setPassword("wrong");

        when(userService.authenticate(eq("alice@usc.edu.ph"), eq("wrong")))
                .thenThrow(new AuthenticationException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    // ── /api/auth/forgot-password ──────────────────────────

    @Test
    @DisplayName("POST /api/auth/forgot-password returns 200 even when service returns silently")
    void forgotPassword_silentForUnknown() throws Exception {
        // Service returns silently for unknown emails (no enumeration)
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ghost@usc.edu.ph\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password rejects empty email")
    void forgotPassword_rejectsEmptyEmail() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
