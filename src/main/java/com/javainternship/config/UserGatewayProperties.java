package com.javainternship.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "user.gateway")
public class UserGatewayProperties {

    /**
     * Shared secret for API Gateway server-side calls (registration + rollback). Must match gateway env GATEWAY_INTERNAL_SECRET.
     */
    private String internalSecret = "";
}
