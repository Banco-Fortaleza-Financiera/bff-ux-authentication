package com.bancofortaleza.authentication.services.impl;

import com.bancofortaleza.authentication.services.LogoutService;
import com.bff.services.client.SupportApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutService {

    private final SupportApiClient supportApiClient;

    @Override
    public void logoutUser(String xDeviceIp, String xSession) {
        supportApiClient.logoutUser(xDeviceIp, xSession);
    }
}
