package com.poliakov.taxplatform.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(

        @NotBlank
        @Email
        @Size(max = 320)
        String email,

        @NotBlank
        @Size(min = 2, max = 100)
        String displayName,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
