package com.bancofortaleza.authentication.services.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.bff.services.client.SupportApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutServiceImplTest {

    @Mock
    private SupportApiClient supportApiClient;

    @InjectMocks
    private LogoutServiceImpl logoutService;

    @Test
    void logoutUser_whenCalled_delegatesToSupportClient() {
        // Arrange
        String xDeviceIp = "192.168.1.10";
        String xSession = "session-123";

        // Act
        logoutService.logoutUser(xDeviceIp, xSession);

        // Assert
        verify(supportApiClient).logoutUser(xDeviceIp, xSession);
        verifyNoMoreInteractions(supportApiClient);
    }

    @Test
    void logoutUser_whenSupportFails_propagatesException() {
        // Arrange
        String xDeviceIp = "192.168.1.10";
        String xSession = "session-123";
        RuntimeException exception = new RuntimeException("support unavailable");

        org.mockito.Mockito.doThrow(exception).when(supportApiClient).logoutUser(xDeviceIp, xSession);

        // Act / Assert
        assertThatThrownBy(() -> logoutService.logoutUser(xDeviceIp, xSession)).isSameAs(exception);
        verify(supportApiClient).logoutUser(xDeviceIp, xSession);
        verifyNoMoreInteractions(supportApiClient);
    }
}
