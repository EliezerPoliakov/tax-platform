package com.poliakov.taxplatform.documents;

import com.poliakov.taxplatform.companies.Company;
import com.poliakov.taxplatform.companies.CompanyMember;
import com.poliakov.taxplatform.companies.CompanyMemberRepository;
import com.poliakov.taxplatform.companies.CompanyRepository;
import com.poliakov.taxplatform.companies.CompanyRole;
import com.poliakov.taxplatform.identity.User;
import com.poliakov.taxplatform.identity.UserRepository;
import com.poliakov.taxplatform.identity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DocumentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyMemberRepository companyMemberRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user1;
    private User user2;
    private Company company1;
    private Company company2;
    private MockHttpSession session1;
    private MockHttpSession session2;

    @BeforeEach
    void setUp() throws Exception {
        user1 = createUser("user1@example.com", "Password123!");
        user2 = createUser("user2@example.com", "Password123!");

        company1 = createCompanyWithMember(user1, "Company 1");
        company2 = createCompanyWithMember(user2, "Company 2");

        session1 = login("user1@example.com", "Password123!");
        session2 = login("user2@example.com", "Password123!");
    }

    @Test
    void uploadDocumentSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "col1,col2\nval1,val2".getBytes());

        mockMvc.perform(multipart("/api/companies/" + company1.getId() + "/documents")
                        .file(file)
                        .with(csrf())
                        .session(session1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFilename").value("test.csv"))
                .andExpect(jsonPath("$.contentType").value("text/csv"))
                .andExpect(jsonPath("$.sizeBytes").value(file.getSize()))
                .andExpect(jsonPath("$.sha256Checksum").isNotEmpty())
                .andExpect(jsonPath("$.integrationType").value("MANUAL_UPLOAD"))
                .andExpect(jsonPath("$.status").value("UPLOADED"));

        assertThat(documentRepository.findAllByCompanyId(company1.getId())).hasSize(1);
    }

    @Test
    void listDocumentsSuccessfully() throws Exception {
        createDocument(company1.getId(), user1.getId(), "doc1.csv");
        createDocument(company1.getId(), user1.getId(), "doc2.csv");

        mockMvc.perform(get("/api/companies/" + company1.getId() + "/documents")
                        .session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].originalFilename").value("doc1.csv"))
                .andExpect(jsonPath("$[1].originalFilename").value("doc2.csv"));
    }

    @Test
    void getDocumentDetailsSuccessfully() throws Exception {
        Document doc = createDocument(company1.getId(), user1.getId(), "doc.csv");

        mockMvc.perform(get("/api/companies/" + company1.getId() + "/documents/" + doc.getId())
                        .session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doc.getId()))
                .andExpect(jsonPath("$.originalFilename").value("doc.csv"));
    }

    @Test
    void rejectUploadByNonMember() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "content".getBytes());

        mockMvc.perform(multipart("/api/companies/" + company1.getId() + "/documents")
                        .file(file)
                        .with(csrf())
                        .session(session2)) // User 2 is not a member of Company 1
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectListByNonMember() throws Exception {
        mockMvc.perform(get("/api/companies/" + company1.getId() + "/documents")
                        .session(session2))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectGetDetailsByNonMember() throws Exception {
        Document doc = createDocument(company1.getId(), user1.getId(), "doc.csv");

        mockMvc.perform(get("/api/companies/" + company1.getId() + "/documents/" + doc.getId())
                        .session(session2))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/companies/" + company1.getId() + "/documents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectUploadWithoutCsrf() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "content".getBytes());

        mockMvc.perform(multipart("/api/companies/" + company1.getId() + "/documents")
                        .file(file)
                        .session(session1))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/companies/" + company1.getId() + "/documents")
                        .file(file)
                        .with(csrf())
                        .session(session1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMPTY_FILE"));
    }

    @Test
    void rejectInvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/api/companies/" + company1.getId() + "/documents")
                        .file(file)
                        .with(csrf())
                        .session(session1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILE_TYPE"));
    }

    @Test
    void rejectTooLargeFile() throws Exception {
        // Default max size is 5MiB, let's create a slightly larger one if we can,
        // or just mock the service/config.
        // For integration test, we can use a smaller limit in application-test.properties or just rely on the default.
        // 6MB is 6 * 1024 * 1024 bytes.
        byte[] largeContent = new byte[6 * 1024 * 1024];

        MockMultipartFile file = new MockMultipartFile(
                "file", "large.csv", "text/csv", largeContent);

        mockMvc.perform(multipart("/api/companies/" + company1.getId() + "/documents")
                        .file(file)
                        .with(csrf())
                        .session(session1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FILE_TOO_LARGE"));
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

    private Document createDocument(Long companyId, Long userId, String filename) {
        Document document = new Document(
                companyId,
                UUID.randomUUID().toString(),
                filename,
                "text/csv",
                100L,
                "fake-checksum",
                IntegrationType.MANUAL_UPLOAD,
                DocumentStatus.UPLOADED,
                userId,
                Instant.now()
        );
        return documentRepository.saveAndFlush(document);
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
}
