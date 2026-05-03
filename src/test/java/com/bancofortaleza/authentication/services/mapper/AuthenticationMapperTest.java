package com.bancofortaleza.authentication.services.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AuthenticationMapperTest {

    private final AuthenticationMapper mapper = Mappers.getMapper(AuthenticationMapper.class);

    @Test
    void toClient_whenServerRequestHasValues_mapsAllFields() {
        // Arrange
        com.bff.services.server.models.AuthenticationRequest serverRequest =
            new com.bff.services.server.models.AuthenticationRequest()
                .idUser(10)
                .password("Str0ngP@ssword");

        // Act
        com.bff.services.client.models.AuthenticationRequest clientRequest = mapper.toClient(serverRequest);

        // Assert
        assertThat(clientRequest.getIdUser()).isEqualTo(10);
        assertThat(clientRequest.getPassword()).isEqualTo("Str0ngP@ssword");
    }

    @Test
    void toServer_whenClientResponseHasValues_mapsAllFields() {
        // Arrange
        com.bff.services.client.models.AuthenticationResponse clientResponse =
            new com.bff.services.client.models.AuthenticationResponse()
                .idUser(10)
                .accessToken("token")
                .tokenType("Bearer")
                .expiresIn(3600);

        // Act
        com.bff.services.server.models.AuthenticationResponse serverResponse = mapper.toServer(clientResponse);

        // Assert
        assertThat(serverResponse.getIdUser()).isEqualTo(10);
        assertThat(serverResponse.getAccessToken()).isEqualTo("token");
        assertThat(serverResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(serverResponse.getExpiresIn()).isEqualTo(3600);
    }

    @Test
    void mapperMethods_whenSourceIsNull_returnNull() {
        // Act / Assert
        assertThat(mapper.toClient(null)).isNull();
        assertThat(mapper.toServer(null)).isNull();
    }
}
