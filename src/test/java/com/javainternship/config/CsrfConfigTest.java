package com.javainternship.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CsrfConfigTest {

    @Test
    void csrfTokenRepository_returnsCookieRepository() {
        CsrfConfig config = new CsrfConfig();

        CsrfTokenRepository repository = config.csrfTokenRepository();

        assertInstanceOf(CookieCsrfTokenRepository.class, repository);
    }
}
