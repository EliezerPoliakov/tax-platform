package com.poliakov.taxplatform.companies;

import java.time.Instant;

public record CompanyResponse(
        Long id,
        String name,
        CompanyRole role,
        Instant createdAt
) {
}
