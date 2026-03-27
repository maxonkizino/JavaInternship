package com.javainternship.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.cards")
@Getter
@Setter
public class PaymentCardLimitProperties {

    private long maxPerUser = 5;
}

