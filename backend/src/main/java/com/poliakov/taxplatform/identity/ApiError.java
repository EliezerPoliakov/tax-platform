package com.poliakov.taxplatform.identity;

public record ApiError(
        String code,
        String message
) {
}
