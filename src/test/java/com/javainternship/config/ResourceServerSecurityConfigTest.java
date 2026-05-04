package com.javainternship.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class ResourceServerSecurityConfigTest {

    @Test
    void jwtDecoder_returnsNimbusDecoder() {
        CsrfTokenRepository csrfTokenRepository = mock(CsrfTokenRepository.class);
        ResourceServerSecurityConfig config = new ResourceServerSecurityConfig(csrfTokenRepository);

        JwtDecoder decoder = config.jwtDecoder("01234567890123456789012345678901");

        assertInstanceOf(NimbusJwtDecoder.class, decoder);
    }
}
