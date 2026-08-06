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
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException();
        }

        Instant now = Instant.now();

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.displayName().trim(),
                UserStatus.ACTIVE,
                now,
                now
        );

        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            // Database uniqueness remains the final protection
            // against concurrent duplicate registrations.
            throw new DuplicateEmailException();
        }

        return new RegistrationResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getDisplayName()
        );
    }
}
