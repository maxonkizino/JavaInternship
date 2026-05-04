package com.javainternship.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "user.gateway")
public class UserGatewayProperties {

    
    private String internalSecret = "";
}
