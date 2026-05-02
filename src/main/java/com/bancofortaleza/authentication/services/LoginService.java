package com.bancofortaleza.authentication.services;

import com.bff.services.server.models.AuthenticationRequest;
import com.bff.services.server.models.AuthenticationResponse;

public interface LoginService {

    AuthenticationResponse authenticateUser(
            String xDeviceIp,
            String xSession,
            AuthenticationRequest authenticationRequest
    );
}
