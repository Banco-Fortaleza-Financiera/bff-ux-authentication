package com.bancofortaleza.authentication.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.bancofortaleza.authentication.services.LoginService;
import com.bancofortaleza.authentication.services.LogoutService;
import com.bff.services.server.models.AuthenticationRequest;
import com.bff.services.server.models.AuthenticationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private LoginService loginService;

    @Mock
    private LogoutService logoutService;

    @InjectMocks
    private UsersController usersController;

    @Test
    void authenticateUser_whenServiceReturnsAuthentication_returnsOkWithBody() {
        // Arrange
        String xDeviceIp = "192.168.1.10";
        String xSession = "session-123";
        AuthenticationRequest request = new AuthenticationRequest()
            .idUser(10)
            .password("Str0ngP@ssword");
        AuthenticationResponse serviceResponse = new AuthenticationResponse()
            .idUser(10)
            .accessToken("token")
            .tokenType("Bearer")
            .expiresIn(3600);

        when(loginService.authenticateUser(xDeviceIp, xSession, request)).thenReturn(serviceResponse);

        // Act
        ResponseEntity<AuthenticationResponse> response =
            usersController.authenticateUser(xDeviceIp, xSession, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(serviceResponse);
        verify(loginService).authenticateUser(xDeviceIp, xSession, request);
        verifyNoMoreInteractions(loginService, logoutService);
    }

    @Test
    void logoutUser_whenServiceCompletes_returnsNoContent() {
        // Arrange
        String xDeviceIp = "192.168.1.10";
        String xSession = "session-123";

        // Act
        ResponseEntity<Void> response = usersController.logoutUser(xDeviceIp, xSession);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(logoutService).logoutUser(xDeviceIp, xSession);
        verifyNoMoreInteractions(loginService, logoutService);
    }
}
