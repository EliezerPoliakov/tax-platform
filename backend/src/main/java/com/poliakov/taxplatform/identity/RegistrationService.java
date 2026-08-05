package com.poliakov.taxplatform.identity;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        String normalizedEmail = request.email()
                .strip()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException();
        }

        Instant now = Instant.now();

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.displayName().strip(),
                UserStatus.ACTIVE,
                now,
                now
        );

        try {
            User savedUser = userRepository.saveAndFlush(user);

            return new RegistrationResponse(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getDisplayName()
            );
        } catch (DataIntegrityViolationException exception) {
            // Database uniqueness remains the final protection
            // against concurrent duplicate registrations.
            throw new DuplicateEmailException();
        }
    }
}
