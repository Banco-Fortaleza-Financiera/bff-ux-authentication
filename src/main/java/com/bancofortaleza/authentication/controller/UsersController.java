package com.bancofortaleza.authentication.controller;

import com.bancofortaleza.authentication.services.LoginService;
import com.bancofortaleza.authentication.services.LogoutService;
import com.bff.services.server.ChannelApi;
import com.bff.services.server.models.AuthenticationRequest;
import com.bff.services.server.models.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UsersController implements ChannelApi {

    private final LoginService loginService;
    private final LogoutService logoutService;

    @Override
    public ResponseEntity<AuthenticationResponse> authenticateUser(
            String xDeviceIp,
            String xSession,
            AuthenticationRequest authenticationRequest
    ) {
        return ResponseEntity.ok(loginService.authenticateUser(xDeviceIp, xSession, authenticationRequest));
    }

    @Override
    public ResponseEntity<Void> logoutUser(String xDeviceIp, String xSession) {
        logoutService.logoutUser(xDeviceIp, xSession);
        return ResponseEntity.noContent().build();
    }
}
