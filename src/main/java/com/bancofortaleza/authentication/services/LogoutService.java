package com.bancofortaleza.authentication.services;

public interface LogoutService {

    void logoutUser(String xDeviceIp, String xSession);
}
