package com.poliakov.taxplatform.companies;

import com.poliakov.taxplatform.identity.User;
import com.poliakov.taxplatform.identity.UserRepository;
import com.poliakov.taxplatform.identity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class CompanyServiceTransactionalTest {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyMemberRepository companyMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        public CompanyMemberRepository mockCompanyMemberRepository(CompanyMemberRepository real) {
            // We want a mock that fails on save to test rollback of Company creation
            CompanyMemberRepository mock = mock(CompanyMemberRepository.class);
            // Delegate all but save
            when(mock.findAllByUserWithCompany(any())).thenAnswer(inv -> real.findAllByUserWithCompany(inv.getArgument(0)));
            when(mock.findByUserAndCompanyId(any(), any())).thenAnswer(inv -> real.findByUserAndCompanyId(inv.getArgument(0), inv.getArgument(1)));
            when(mock.existsByUserAndCompanyId(any(), any())).thenAnswer(inv -> real.existsByUserAndCompanyId(inv.getArgument(0), inv.getArgument(1)));
            
            // Fail on save
            when(mock.save(any())).thenThrow(new RuntimeException("Simulated membership creation failure"));
            
            return mock;
        }
    }

    @Test
    void companyCreationRollsBackIfMembershipFails() {
        User user = createUser("rollback-" + UUID.randomUUID() + "@example.com");
        
        CreateCompanyRequest request = new CreateCompanyRequest("Should Rollback");
        
        assertThrows(RuntimeException.class, () -> companyService.createCompany(user, request));
        
        assertThat(companyRepository.findAll()).isEmpty();
    }

    private User createUser(String email) {
        Instant now = Instant.now();
        User user = new User(
                email,
                passwordEncoder.encode("password"),
                "Test User",
                UserStatus.ACTIVE,
                now,
                now
        );
        return userRepository.saveAndFlush(user);
    }
}
