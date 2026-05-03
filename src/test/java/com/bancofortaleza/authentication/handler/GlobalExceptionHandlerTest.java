package com.bancofortaleza.authentication.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bancofortaleza.authentication.domain.exceptions.ApiException;
import com.bancofortaleza.authentication.domain.model.ErrorResponse;
import feign.FeignException;
import feign.Request;
import feign.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleFeignException_whenDownstreamStatusIsValid_preservesStatusBodyAndEndToEndHeaders() {
        // Arrange
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://support/auth",
            Map.of(),
            null,
            StandardCharsets.UTF_8,
            null
        );
        Response response = Response.builder()
            .request(request)
            .status(HttpStatus.UNAUTHORIZED.value())
            .reason("Unauthorized")
            .headers(Map.of(
                "x-trace-id", List.of("trace-123"),
                HttpHeaders.CONNECTION, List.of("close"),
                HttpHeaders.CONTENT_LENGTH, List.of("999")
            ))
            .body("unauthorized", StandardCharsets.UTF_8)
            .build();
        FeignException exception = FeignException.errorStatus("SupportApi#authenticateUser", response);

        // Act
        var handledResponse = handler.handleFeignException(exception);

        // Assert
        assertThat(handledResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(new String(handledResponse.getBody(), StandardCharsets.UTF_8)).isEqualTo("unauthorized");
        assertThat(handledResponse.getHeaders().getFirst("x-trace-id")).isEqualTo("trace-123");
        assertThat(handledResponse.getHeaders()).doesNotContainKey(HttpHeaders.CONNECTION);
        assertThat(handledResponse.getHeaders()).doesNotContainKey(HttpHeaders.CONTENT_LENGTH);
    }

    @Test
    void handleFeignException_whenDownstreamStatusIsInvalid_returnsBadGatewayWithoutBody() {
        // Arrange
        FeignException exception = mock(FeignException.class);
        when(exception.status()).thenReturn(700);

        // Act
        var response = handler.handleFeignException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void handleFeignException_whenDownstreamStatusIsTooLow_returnsBadGatewayWithoutBody() {
        // Arrange
        FeignException exception = mock(FeignException.class);
        when(exception.status()).thenReturn(99);

        // Act
        var response = handler.handleFeignException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void handleFeignException_whenHeadersHaveNullEntries_ignoresInvalidHeaders() {
        // Arrange
        FeignException exception = mock(FeignException.class);
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put(null, List.of("ignored"));
        headers.put("x-null-values", null);
        headers.put("x-safe-header", List.of("safe"));

        when(exception.status()).thenReturn(HttpStatus.BAD_GATEWAY.value());
        when(exception.responseHeaders()).thenReturn(headers);
        when(exception.content()).thenReturn("bad gateway".getBytes(StandardCharsets.UTF_8));

        // Act
        var response = handler.handleFeignException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getHeaders().getFirst("x-safe-header")).isEqualTo("safe");
        assertThat(response.getHeaders()).doesNotContainKey("x-null-values");
    }

    @Test
    void handleApiException_whenApiExceptionIsHandled_returnsConfiguredErrorResponse() {
        // Arrange
        HttpServletRequest request = requestWithUri("/channel/authenticate");
        ApiException exception = new ApiException(HttpStatus.CONFLICT, "USER_LOCKED", "User is locked");

        // Act
        var response = handler.handleApiException(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody())
            .extracting(ErrorResponse::status, ErrorResponse::error, ErrorResponse::code, ErrorResponse::message,
                ErrorResponse::path)
            .containsExactly(409, "Conflict", "USER_LOCKED", "User is locked", "/channel/authenticate");
        assertThat(response.getBody().details()).isEmpty();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleValidation_whenFieldsAreInvalid_returnsPublicFieldNames() throws NoSuchMethodException {
        // Arrange
        HttpServletRequest request = requestWithUri("/channel/authenticate");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
            new com.bff.services.server.models.AuthenticationRequest(),
            "authenticationRequest"
        );
        bindingResult.addError(new FieldError("authenticationRequest", "xDeviceIp", "must not be blank"));
        bindingResult.addError(new FieldError("authenticationRequest", "password", "size must be between 8 and 128"));
        bindingResult.addError(new FieldError("authenticationRequest", "credentials.", "invalid"));
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod(
            "validationTarget",
            com.bff.services.server.models.AuthenticationRequest.class
        );
        MethodArgumentNotValidException exception =
            new MethodArgumentNotValidException(new MethodParameter(method, 0), bindingResult);

        // Act
        var response = handler.handleValidation(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).isEqualTo("Request validation failed");
        assertThat(response.getBody().details())
            .containsExactly(
                new ErrorResponse.FieldError("x-device-ip", "must not be blank"),
                new ErrorResponse.FieldError("password", "size must be between 8 and 128"),
                new ErrorResponse.FieldError("credentials.", "invalid")
            );
    }

    @Test
    void handleConstraintViolation_whenHeadersAreInvalid_returnsPublicFieldNames() {
        // Arrange
        HttpServletRequest request = requestWithUri("/channel/logout");
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("logoutUser.xSession");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        // Act
        var response = handler.handleConstraintViolation(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().details())
            .containsExactly(new ErrorResponse.FieldError("x-session", "must not be blank"));
    }

    @Test
    void handleBadRequest_whenRequestCannotBeRead_returnsBadRequestError() {
        // Arrange
        HttpServletRequest request = requestWithUri("/channel/authenticate");
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
            "invalid body",
            new MockHttpInputMessage(new byte[0])
        );

        // Act
        var response = handler.handleBadRequest(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("Invalid request");
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void handleMissingRequestParameter_whenParameterIsMissing_returnsFieldError() {
        // Arrange
        HttpServletRequest request = requestWithUri("/channel/authenticate");
        MissingServletRequestParameterException exception =
            new MissingServletRequestParameterException("idUser", "Integer");

        // Act
        var response = handler.handleMissingRequestParameter(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("MISSING_REQUIRED_PARAMETER");
        assertThat(response.getBody().message()).isEqualTo("Required parameter 'idUser' is missing");
        assertThat(response.getBody().details())
            .containsExactly(new ErrorResponse.FieldError("idUser", "Parameter is required"));
    }

    @Test
    void handleMissingRequestHeader_whenHeaderIsMissing_returnsFieldError() {
        // Arrange
        HttpServletRequest request = requestWithUri("/channel/logout");
        MissingRequestHeaderException exception = new MissingRequestHeaderException("x-session", null);

        // Act
        var response = handler.handleMissingRequestHeader(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("MISSING_REQUIRED_HEADER");
        assertThat(response.getBody().message()).isEqualTo("Required header 'x-session' is missing");
        assertThat(response.getBody().details())
            .containsExactly(new ErrorResponse.FieldError("x-session", "Header is required"));
    }

    @Test
    void handleNotFound_whenResourceDoesNotExist_returnsNotFoundError() {
        // Arrange
        HttpServletRequest request = requestWithUri("/missing");

        // Act
        var response = handler.handleNotFound(new RuntimeException("not found"), request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().path()).isEqualTo("/missing");
    }

    @Test
    void handleMethodNotAllowed_whenHttpMethodIsUnsupported_returnsMethodNotAllowedError() {
        // Arrange
        HttpServletRequest request = requestWithUri("/channel/logout");
        HttpRequestMethodNotSupportedException exception =
            new HttpRequestMethodNotSupportedException("PATCH");

        // Act
        var response = handler.handleMethodNotAllowed(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody().code()).isEqualTo("METHOD_NOT_ALLOWED");
        assertThat(response.getBody().message()).isEqualTo("Method not allowed");
    }

    @Test
    void handleUnexpected_whenExceptionIsUnhandled_returnsInternalServerError() {
        // Arrange
        HttpServletRequest request = requestWithUri("/channel/authenticate");

        // Act
        var response = handler.handleUnexpected(new RuntimeException("boom"), request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().message()).isEqualTo("Unexpected error");
    }

    private HttpServletRequest requestWithUri(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }

    @SuppressWarnings("unused")
    private void validationTarget(com.bff.services.server.models.AuthenticationRequest authenticationRequest) {
    }
}
