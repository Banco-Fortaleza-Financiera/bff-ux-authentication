package com.bancofortaleza.authentication.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppLoggerTest {

    @BeforeEach
    void setUp() {
        AppLogger.clearContext();
    }

    @AfterEach
    void tearDown() {
        AppLogger.clearContext();
    }

    @Test
    void putContext_whenValueIsPresent_storesValueInMdc() {
        // Act
        AppLogger.putContext(AppLogger.REQUEST_ID, "request-123");

        // Assert
        assertThat(AppLogger.getContext(AppLogger.REQUEST_ID)).isEqualTo("request-123");
    }

    @Test
    void putContext_whenValueIsNullOrBlank_doesNotStoreValueInMdc() {
        // Act
        AppLogger.putContext("nullValue", null);
        AppLogger.putContext("blankValue", " ");

        // Assert
        assertThat(AppLogger.getContext("nullValue")).isNull();
        assertThat(AppLogger.getContext("blankValue")).isNull();
    }

    @Test
    void putContext_whenMapHasValues_storesOnlyNonBlankValues() {
        // Act
        AppLogger.putContext(Map.of(
            AppLogger.METHOD, "POST",
            AppLogger.PATH, "/channel/authenticate",
            "blankValue", ""
        ));

        // Assert
        assertThat(AppLogger.getContext(AppLogger.METHOD)).isEqualTo("POST");
        assertThat(AppLogger.getContext(AppLogger.PATH)).isEqualTo("/channel/authenticate");
        assertThat(AppLogger.getContext("blankValue")).isNull();
    }

    @Test
    void removeContext_whenKeyExists_removesValueFromMdc() {
        // Arrange
        AppLogger.putContext(AppLogger.DEVICE_IP, "192.168.1.10");

        // Act
        AppLogger.removeContext(AppLogger.DEVICE_IP);

        // Assert
        assertThat(AppLogger.getContext(AppLogger.DEVICE_IP)).isNull();
    }

    @Test
    void clearContext_whenValuesExist_removesAllValuesFromMdc() {
        // Arrange
        AppLogger.putContext(AppLogger.REQUEST_ID, "request-123");
        AppLogger.putContext(AppLogger.METHOD, "POST");

        // Act
        AppLogger.clearContext();

        // Assert
        assertThat(AppLogger.getContext(AppLogger.REQUEST_ID)).isNull();
        assertThat(AppLogger.getContext(AppLogger.METHOD)).isNull();
    }

    @Test
    void loggingMethods_whenCalled_doNotThrowException() {
        // Act / Assert
        assertThatCode(() -> {
            AppLogger.trace(AppLoggerTest.class, "trace {}", "message");
            AppLogger.debug(AppLoggerTest.class, "debug {}", "message");
            AppLogger.info(AppLoggerTest.class, "info {}", "message");
            AppLogger.warn(AppLoggerTest.class, "warn {}", "message");
            AppLogger.warn(AppLoggerTest.class, "warn with throwable", new RuntimeException("warn"));
            AppLogger.error(AppLoggerTest.class, "error {}", "message");
            AppLogger.error(AppLoggerTest.class, "error with throwable", new RuntimeException("error"));
            AppLogger.requestStarted(AppLoggerTest.class, "GET", "/health");
            AppLogger.requestCompleted(AppLoggerTest.class, "GET", "/health", 200, 15L);
            AppLogger.requestFailed(AppLoggerTest.class, "GET", "/health", 15L, new RuntimeException("boom"));
        }).doesNotThrowAnyException();
    }
}
