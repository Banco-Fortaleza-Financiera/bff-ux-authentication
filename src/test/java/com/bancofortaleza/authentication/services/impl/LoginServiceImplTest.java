package com.bancofortaleza.authentication.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.bancofortaleza.authentication.services.mapper.AuthenticationMapper;
import com.bff.services.client.SupportApiClient;
import com.bff.services.server.models.AuthenticationRequest;
import com.bff.services.server.models.AuthenticationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    private SupportApiClient supportApiClient;

    @Mock
    private AuthenticationMapper authenticationMapper;

    @InjectMocks
    private LoginServiceImpl loginService;

    @Test
    void authenticateUser_whenSupportReturnsResponse_mapsAndReturnsAuthenticationResponse() {
        // Arrange
        String xDeviceIp = "192.168.1.10";
        String xSession = "session-123";
        AuthenticationRequest serverRequest = new AuthenticationRequest()
            .idUser(10)
            .password("Str0ngP@ssword");
        com.bff.services.client.models.AuthenticationRequest clientRequest =
            new com.bff.services.client.models.AuthenticationRequest()
                .idUser(10)
                .password("Str0ngP@ssword");
        com.bff.services.client.models.AuthenticationResponse clientResponse =
            new com.bff.services.client.models.AuthenticationResponse()
                .idUser(10)
                .accessToken("token")
                .tokenType("Bearer")
                .expiresIn(3600);
        AuthenticationResponse expectedResponse = new AuthenticationResponse()
            .idUser(10)
            .accessToken("token")
            .tokenType("Bearer")
            .expiresIn(3600);

        when(authenticationMapper.toClient(serverRequest)).thenReturn(clientRequest);
        when(supportApiClient.authenticateUser(xDeviceIp, xSession, clientRequest))
            .thenReturn(ResponseEntity.ok(clientResponse));
        when(authenticationMapper.toServer(clientResponse)).thenReturn(expectedResponse);

        // Act
        AuthenticationResponse response = loginService.authenticateUser(xDeviceIp, xSession, serverRequest);

        // Assert
        assertThat(response).isSameAs(expectedResponse);
        verify(authenticationMapper).toClient(serverRequest);
        verify(supportApiClient).authenticateUser(xDeviceIp, xSession, clientRequest);
        verify(authenticationMapper).toServer(clientResponse);
        verifyNoMoreInteractions(authenticationMapper, supportApiClient);
    }

    @Test
    void authenticateUser_whenSupportFails_propagatesException() {
        // Arrange
        String xDeviceIp = "192.168.1.10";
        String xSession = "session-123";
        AuthenticationRequest serverRequest = new AuthenticationRequest()
            .idUser(10)
            .password("Str0ngP@ssword");
        com.bff.services.client.models.AuthenticationRequest clientRequest =
            new com.bff.services.client.models.AuthenticationRequest()
                .idUser(10)
                .password("Str0ngP@ssword");
        RuntimeException exception = new RuntimeException("support unavailable");

        when(authenticationMapper.toClient(serverRequest)).thenReturn(clientRequest);
        when(supportApiClient.authenticateUser(xDeviceIp, xSession, clientRequest)).thenThrow(exception);

        // Act / Assert
        assertThatThrownBy(() -> loginService.authenticateUser(xDeviceIp, xSession, serverRequest))
            .isSameAs(exception);

        verify(authenticationMapper).toClient(serverRequest);
        verify(supportApiClient).authenticateUser(xDeviceIp, xSession, clientRequest);
        verifyNoMoreInteractions(authenticationMapper, supportApiClient);
    }
}
