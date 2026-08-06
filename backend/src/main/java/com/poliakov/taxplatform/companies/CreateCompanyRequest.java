package com.poliakov.taxplatform.companies;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank
        @Size(max = 255)
        String name
) {
}
