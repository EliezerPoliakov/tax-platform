package com.poliakov.taxplatform.processing;

import com.poliakov.taxplatform.companies.Company;
import com.poliakov.taxplatform.companies.CompanyMember;
import com.poliakov.taxplatform.companies.CompanyMemberRepository;
import com.poliakov.taxplatform.companies.CompanyRepository;
import com.poliakov.taxplatform.companies.CompanyRole;
import com.poliakov.taxplatform.documents.Document;
import com.poliakov.taxplatform.documents.DocumentRepository;
import com.poliakov.taxplatform.documents.DocumentStatus;
import com.poliakov.taxplatform.documents.DocumentStorage;
import com.poliakov.taxplatform.documents.IntegrationType;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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
class ProcessingJobApiTest {

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
    private ProcessingJobRepository processingJobRepository;

    @Autowired
    private CanonicalRecordRepository canonicalRecordRepository;

    @Autowired
    private DocumentStorage documentStorage;

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
    void processValidCsvSuccessfully() throws Exception {
        String csv = "document_number,document_date,currency,amount\nINV-001,2026-01-01,USD,100.00";
        Document doc = createDocument(company1.getId(), user1.getId(), "valid.csv", csv);

        mockMvc.perform(post("/api/companies/" + company1.getId() + "/documents/" + doc.getId() + "/processing-jobs")
                        .with(csrf())
                        .session(session1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.resultSummary").isNotEmpty())
                .andExpect(jsonPath("$.failureCode").isEmpty());

        var jobs = processingJobRepository.findAllByCompanyId(company1.getId());
        assertThat(jobs).hasSize(1);
        assertThat(canonicalRecordRepository.findAllByJobId(jobs.get(0).getId())).hasSize(1);
    }

    @Test
    void processInvalidDateFails() throws Exception {
        String csv = "document_number,document_date,currency,amount\nINV-001,2026-13-01,USD,100.00";
        Document doc = createDocument(company1.getId(), user1.getId(), "invalid_date.csv", csv);

        mockMvc.perform(post("/api/companies/" + company1.getId() + "/documents/" + doc.getId() + "/processing-jobs")
                        .with(csrf())
                        .session(session1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failureCode").value("INVALID_DOCUMENT_DATE"));

        var jobs = processingJobRepository.findAllByCompanyId(company1.getId());
        assertThat(canonicalRecordRepository.findAllByJobId(jobs.get(0).getId())).isEmpty();
    }

    @Test
    void processInvalidAmountFails() throws Exception {
        String csv = "document_number,document_date,currency,amount\nINV-001,2026-01-01,USD,abc";
        Document doc = createDocument(company1.getId(), user1.getId(), "invalid_amount.csv", csv);

        mockMvc.perform(post("/api/companies/" + company1.getId() + "/documents/" + doc.getId() + "/processing-jobs")
                        .with(csrf())
                        .session(session1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failureCode").value("INVALID_AMOUNT"));

        var jobs = processingJobRepository.findAllByCompanyId(company1.getId());
        assertThat(canonicalRecordRepository.findAllByJobId(jobs.get(0).getId())).isEmpty();
    }

    @Test
    void processInvalidHeaderFails() throws Exception {
        String csv = "wrong,document_date,currency,amount\nINV-001,2026-01-01,USD,100.00";
        Document doc = createDocument(company1.getId(), user1.getId(), "invalid_header.csv", csv);

        mockMvc.perform(post("/api/companies/" + company1.getId() + "/documents/" + doc.getId() + "/processing-jobs")
                        .with(csrf())
                        .session(session1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failureCode").value("INVALID_CSV_HEADER"));
    }

    @Test
    void rejectCrossTenantProcessing() throws Exception {
        String csv = "document_number,document_date,currency,amount\nINV-001,2026-01-01,USD,100.00";
        Document doc = createDocument(company1.getId(), user1.getId(), "valid.csv", csv);

        // User 2 tries to process Document 1 (which belongs to Company 1) via Company 2
        mockMvc.perform(post("/api/companies/" + company2.getId() + "/documents/" + doc.getId() + "/processing-jobs")
                        .with(csrf())
                        .session(session2))
                .andExpect(status().isBadRequest()); // DocumentService throws IAE if not found in company
    }

    @Test
    void rejectCrossTenantJobRetrieval() throws Exception {
        String csv = "document_number,document_date,currency,amount\nINV-001,2026-01-01,USD,100.00";
        Document doc = createDocument(company1.getId(), user1.getId(), "valid.csv", csv);
        
        // Create job for Company 1
        ProcessingJob job = new ProcessingJob(company1.getId(), doc.getId(), "SYNTHETIC_CSV", "v1", "corr1");
        job = processingJobRepository.saveAndFlush(job);

        // User 2 tries to get Job 1 via Company 2
        mockMvc.perform(get("/api/companies/" + company2.getId() + "/processing-jobs/" + job.getId())
                        .session(session2))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/companies/1/documents/1/processing-jobs")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/companies/" + company1.getId() + "/documents/1/processing-jobs")
                        .session(session1))
                .andExpect(status().isForbidden());
    }

    private User createUser(String email, String password) {
        Instant now = Instant.now();
        User user = new User(email, passwordEncoder.encode(password), "Test User", UserStatus.ACTIVE, now, now);
        return userRepository.saveAndFlush(user);
    }

    private Company createCompanyWithMember(User user, String name) {
        Instant now = Instant.now();
        Company company = companyRepository.saveAndFlush(new Company(name, now, now));
        companyMemberRepository.saveAndFlush(new CompanyMember(user, company, CompanyRole.OWNER, now, now));
        return company;
    }

    private Document createDocument(Long companyId, Long userId, String filename, String content) {
        String storageKey = UUID.randomUUID().toString();
        documentStorage.store(storageKey, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        Document document = new Document(
                companyId,
                storageKey,
                filename,
                "text/csv",
                (long) content.length(),
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
                        .content("{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }
}
