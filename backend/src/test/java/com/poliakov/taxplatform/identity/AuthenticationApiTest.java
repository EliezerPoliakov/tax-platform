package com.poliakov.taxplatform.identity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void successfulLogin() throws Exception {
        String email = uniqueEmail();
        String password = "StrongPassword123!";
        createUser(email, password, "Test User", UserStatus.ACTIVE);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value("Test User"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void emailNormalizationDuringLogin() throws Exception {
        String email = uniqueEmail();
        String password = "StrongPassword123!";
        createUser(email.toLowerCase(), password, "Test User", UserStatus.ACTIVE);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": " %s ",
                                  "password": "%s"
                                }
                                """.formatted(email.toUpperCase(), password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email.toLowerCase()));
    }

    @Test
    void sameErrorForWrongPasswordAndUnknownEmail() throws Exception {
        String email = uniqueEmail();
        String password = "StrongPassword123!";
        createUser(email, password, "Test User", UserStatus.ACTIVE);

        // Wrong password
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "wrong-password"
                                }
                                """.formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

        // Unknown email
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "unknown@example.com",
                                  "password": "%s"
                                }
                                """.formatted(password)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void rejectsInactiveUser() throws Exception {
        String email = uniqueEmail();
        String password = "StrongPassword123!";
        createUser(email, password, "Test User", UserStatus.DISABLED);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void rejectsLoginWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "any@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentUser() throws Exception {
        String email = uniqueEmail();
        String password = "StrongPassword123!";
        createUser(email, password, "Test User", UserStatus.ACTIVE);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();

        mockMvc.perform(get("/api/auth/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value("Test User"));
    }

    @Test
    void getMeReturns401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        String email = uniqueEmail();
        String password = "StrongPassword123!";
        createUser(email, password, "Test User", UserStatus.ACTIVE);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me")
                        .session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRejectedWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden());
    }

    private void createUser(String email, String password, String displayName, UserStatus status) {
        Instant now = Instant.now();
        User user = new User(
                email,
                passwordEncoder.encode(password),
                displayName,
                status,
                now,
                now
        );
        userRepository.saveAndFlush(user);
    }

    private static String uniqueEmail() {
        return "auth-" + UUID.randomUUID() + "@example.com";
    }
}
