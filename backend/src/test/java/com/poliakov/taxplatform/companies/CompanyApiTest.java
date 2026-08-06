package com.poliakov.taxplatform.companies;

import com.poliakov.taxplatform.identity.User;
import com.poliakov.taxplatform.identity.UserRepository;
import com.poliakov.taxplatform.identity.UserStatus;
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
class CompanyApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyMemberRepository companyMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void authenticatedUserCreatesCompanySuccessfully() throws Exception {
        String email = uniqueEmail();
        String password = "Password123!";
        User user = createUser(email, password);

        MockHttpSession session = login(email, password);

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test Company"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Company"))
                .andExpect(jsonPath("$.role").value("OWNER"));

        assertThat(companyRepository.findAll()).hasSize(1);
        Company company = companyRepository.findAll().get(0);
        assertThat(company.getName()).isEqualTo("Test Company");

        assertThat(companyMemberRepository.findAll()).hasSize(1);
        CompanyMember member = companyMemberRepository.findAll().get(0);
        assertThat(member.getUser().getId()).isEqualTo(user.getId());
        assertThat(member.getCompany().getId()).isEqualTo(company.getId());
        assertThat(member.getRole()).isEqualTo(CompanyRole.OWNER);
    }

    @Test
    void listOnlyAccessibleCompanies() throws Exception {
        User user1 = createUser(uniqueEmail(), "Password123!");
        User user2 = createUser(uniqueEmail(), "Password123!");

        createCompanyWithMember(user1, "User 1 Company");
        createCompanyWithMember(user2, "User 2 Company");

        MockHttpSession session1 = login(user1.getEmail(), "Password123!");

        mockMvc.perform(get("/api/companies")
                        .session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("User 1 Company"));
    }

    @Test
    void getCompanyDetailsOnlyForMember() throws Exception {
        User user1 = createUser(uniqueEmail(), "Password123!");
        User user2 = createUser(uniqueEmail(), "Password123!");

        Company company1 = createCompanyWithMember(user1, "User 1 Company");

        MockHttpSession session1 = login(user1.getEmail(), "Password123!");
        mockMvc.perform(get("/api/companies/" + company1.getId())
                        .session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("User 1 Company"));

        MockHttpSession session2 = login(user2.getEmail(), "Password123!");
        mockMvc.perform(get("/api/companies/" + company1.getId())
                        .session(session2))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Fail\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectCreationWithoutCsrf() throws Exception {
        String email = uniqueEmail();
        createUser(email, "Password123!");
        MockHttpSession session = login(email, "Password123!");

        mockMvc.perform(post("/api/companies")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"No CSRF\"}"))
                .andExpect(status().isForbidden());
    }

    private User createUser(String email, String password) {
        Instant now = Instant.now();
        User user = new User(
                email,
                passwordEncoder.encode(password),
                "Test User",
                UserStatus.ACTIVE,
                now,
                now
        );
        return userRepository.saveAndFlush(user);
    }

    private Company createCompanyWithMember(User user, String name) {
        Instant now = Instant.now();
        Company company = companyRepository.saveAndFlush(new Company(name, now, now));
        companyMemberRepository.saveAndFlush(new CompanyMember(user, company, CompanyRole.OWNER, now, now));
        return company;
    }

    private MockHttpSession login(String email, String password) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
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
    }

    private static String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }
}
