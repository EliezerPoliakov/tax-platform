package com.poliakov.taxplatform.identity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsUserByEmail() {
        String email = uniqueEmail();
        Instant now = Instant.now();

        User user = new User(
                email,
                "test-password-hash",
                "Test User",
                UserStatus.ACTIVE,
                now,
                now
        );

        User savedUser = userRepository.saveAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(userRepository.existsByEmail(email)).isTrue();

        assertThat(userRepository.findByEmail(email))
                .isPresent()
                .get()
                .satisfies(foundUser -> {
                    assertThat(foundUser.getEmail()).isEqualTo(email);
                    assertThat(foundUser.getPasswordHash())
                            .isEqualTo("test-password-hash");
                    assertThat(foundUser.getDisplayName())
                            .isEqualTo("Test User");
                    assertThat(foundUser.getStatus())
                            .isEqualTo(UserStatus.ACTIVE);
                    assertThat(foundUser.getCreatedAt()).isNotNull();
                    assertThat(foundUser.getUpdatedAt()).isNotNull();
                });
    }

    @Test
    void rejectsDuplicateEmail() {
        String email = uniqueEmail();
        Instant now = Instant.now();

        User firstUser = new User(
                email,
                "first-password-hash",
                "First User",
                UserStatus.ACTIVE,
                now,
                now
        );

        User secondUser = new User(
                email,
                "second-password-hash",
                "Second User",
                UserStatus.ACTIVE,
                now,
                now
        );

        userRepository.saveAndFlush(firstUser);

        assertThatThrownBy(() -> userRepository.saveAndFlush(secondUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static String uniqueEmail() {
        return "repository-" + UUID.randomUUID() + "@example.com";
    }
}
