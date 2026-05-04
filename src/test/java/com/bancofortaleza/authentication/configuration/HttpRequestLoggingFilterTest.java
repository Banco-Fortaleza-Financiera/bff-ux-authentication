package com.bancofortaleza.authentication.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancofortaleza.authentication.utils.AppLogger;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class HttpRequestLoggingFilterTest {

    private final HttpRequestLoggingFilter filter = new HttpRequestLoggingFilter();

    @AfterEach
    void tearDown() {
        AppLogger.clearContext();
    }

    @Test
    void doFilter_whenRequestHasRequestId_usesExistingRequestIdAndClearsContext() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        request.addHeader("x-request-id", "request-123");
        request.addHeader("x-device-ip", "192.168.1.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilter(request, response, new MockFilterChain());

        // Assert
        assertThat(response.getHeader("x-request-id")).isEqualTo("request-123");
        assertThat(AppLogger.getContext(AppLogger.REQUEST_ID)).isNull();
        assertThat(AppLogger.getContext(AppLogger.DEVICE_IP)).isNull();
    }

    @Test
    void doFilter_whenRequestIdIsMissing_generatesRequestIdAndClearsContext() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/channel/authenticate");
        request.setRemoteAddr("10.0.0.5");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilter(request, response, new MockFilterChain());

        // Assert
        assertThat(response.getHeader("x-request-id")).isNotBlank();
        assertThat(AppLogger.getContext(AppLogger.REQUEST_ID)).isNull();
        assertThat(AppLogger.getContext(AppLogger.DEVICE_IP)).isNull();
    }

    @Test
    void doFilter_whenFilterChainFails_propagatesExceptionAndClearsContext() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletException exception = new ServletException("boom");

        // Act / Assert
        assertThatThrownBy(() -> filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw exception;
        }))
            .isSameAs(exception);
        assertThat(AppLogger.getContext(AppLogger.REQUEST_ID)).isNull();
    }
}
