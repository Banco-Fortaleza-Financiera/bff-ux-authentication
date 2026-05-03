package com.bancofortaleza.authentication.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {

    @Test
    void errorResponse_whenCreated_exposesAllValues() {
        // Arrange
        Instant timestamp = Instant.parse("2026-05-03T16:00:00Z");
        ErrorResponse.FieldError detail = new ErrorResponse.FieldError("x-session", "Header is required");

        // Act
        ErrorResponse response = new ErrorResponse(
            timestamp,
            400,
            "Bad Request",
            "MISSING_REQUIRED_HEADER",
            "Required header 'x-session' is missing",
            "/channel/logout",
            List.of(detail)
        );

        // Assert
        assertThat(response.timestamp()).isEqualTo(timestamp);
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.error()).isEqualTo("Bad Request");
        assertThat(response.code()).isEqualTo("MISSING_REQUIRED_HEADER");
        assertThat(response.message()).isEqualTo("Required header 'x-session' is missing");
        assertThat(response.path()).isEqualTo("/channel/logout");
        assertThat(response.details()).containsExactly(detail);
        assertThat(detail.field()).isEqualTo("x-session");
        assertThat(detail.message()).isEqualTo("Header is required");
    }
}
