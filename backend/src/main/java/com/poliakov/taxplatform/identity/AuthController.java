package com.poliakov.taxplatform.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    public AuthController(
            RegistrationService registrationService,
            UserRepository userRepository,
            AuthenticationManager authenticationManager
    ) {
        this.registrationService = registrationService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request
    ) {
        RegistrationResponse response = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticatedUserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        try {
            UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.unauthenticated(
                    normalizedEmail, request.password());
            Authentication authentication = authenticationManager.authenticate(token);

            SecurityContext context = securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(authentication);
            securityContextHolderStrategy.setContext(context);
            securityContextRepository.saveContext(context, httpServletRequest, httpServletResponse);

            User user = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new IllegalStateException("User authenticated but not found in repository"));

            return ResponseEntity.ok(new AuthenticatedUserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getDisplayName()
            ));
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new InvalidCredentialsException();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        return ResponseEntity.ok(new AuthenticatedUserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        ));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(
            DuplicateEmailException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        "EMAIL_ALREADY_REGISTERED",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(
                        "INVALID_CREDENTIALS",
                        "Invalid email or password"
                ));
    }
}
