package com.poliakov.taxplatform.documents;

import com.poliakov.taxplatform.companies.MembershipService;
import com.poliakov.taxplatform.identity.ApiError;
import com.poliakov.taxplatform.identity.User;
import com.poliakov.taxplatform.identity.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;
    private final MembershipService membershipService;
    private final UserRepository userRepository;

    public DocumentController(
            DocumentService documentService,
            MembershipService membershipService,
            UserRepository userRepository
    ) {
        this.documentService = documentService;
        this.membershipService = membershipService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);
        verifyMembership(user, companyId);

        Document document = documentService.uploadDocument(companyId, user, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponse.fromEntity(document));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> listDocuments(
            @PathVariable Long companyId,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);
        verifyMembership(user, companyId);

        List<DocumentResponse> documents = documentService.listDocuments(companyId).stream()
                .map(DocumentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable Long companyId,
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);
        verifyMembership(user, companyId);

        return documentService.getDocument(documentId, companyId)
                .map(document -> ResponseEntity.ok(DocumentResponse.fromEntity(document)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private void verifyMembership(User user, Long companyId) {
        if (!membershipService.isMember(user, companyId)) {
            // Per requirement: resource non-disclosure conventions.
            // Often this means returning 404 instead of 403 to not leak existence of company/resource.
            // But usually for company-level access, 403 is also common if the company ID is known.
            // AGENTS.md says: "reject access from a non-member even when the resource identifier is known"
            // Let's use 403 or 404 based on project conventions.
            // AuthController handles InvalidCredentials with 401.
            // For now, let's use a custom AccessDeniedException that we can map.
            throw new AccessDeniedException();
        }
    }

    @ExceptionHandler(DocumentException.class)
    public ResponseEntity<ApiError> handleDocumentException(DocumentException e) {
        log.warn("Document operation failed: {} - {}", e.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("ACCESS_DENIED", "You do not have permission to access this resource"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("UNAUTHORIZED", "Full authentication is required to access this resource"));
    }

    private static class AccessDeniedException extends RuntimeException {}
    private static class UnauthorizedException extends RuntimeException {}
}
