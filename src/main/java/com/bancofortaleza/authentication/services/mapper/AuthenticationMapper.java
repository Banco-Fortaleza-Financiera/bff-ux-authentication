package com.bancofortaleza.authentication.services.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthenticationMapper {

    com.bff.services.client.models.AuthenticationRequest toClient(
            com.bff.services.server.models.AuthenticationRequest authenticationRequest
    );

    com.bff.services.server.models.AuthenticationResponse toServer(
            com.bff.services.client.models.AuthenticationResponse authenticationResponse
    );
}
