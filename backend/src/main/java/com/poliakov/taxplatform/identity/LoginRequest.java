package com.poliakov.taxplatform.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Size(max = 320)
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
