package com.bancofortaleza.authentication.services.impl;

import com.bancofortaleza.authentication.services.LoginService;
import com.bancofortaleza.authentication.services.mapper.AuthenticationMapper;
import com.bff.services.client.SupportApiClient;
import com.bff.services.server.models.AuthenticationRequest;
import com.bff.services.server.models.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final SupportApiClient supportApiClient;
    private final AuthenticationMapper authenticationMapper;

    @Override
    public AuthenticationResponse authenticateUser(
            String xDeviceIp,
            String xSession,
            AuthenticationRequest authenticationRequest
    ) {
        var clientRequest = authenticationMapper.toClient(authenticationRequest);
        var clientResponse = supportApiClient.authenticateUser(xDeviceIp, xSession, clientRequest);

        return authenticationMapper.toServer(clientResponse.getBody());
    }
}
