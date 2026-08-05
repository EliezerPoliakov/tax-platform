package com.poliakov.taxplatform.identity;

public record RegistrationResponse(
        Long id,
        String email,
        String displayName
) {
}
