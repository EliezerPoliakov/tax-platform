package com.poliakov.taxplatform.identity;

public record AuthenticatedUserResponse(
        Long id,
        String email,
        String displayName
) {
}
