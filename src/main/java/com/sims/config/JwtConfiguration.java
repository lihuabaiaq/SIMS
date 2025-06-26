package com.sims.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "sims.jwt")
@Data
public class JwtConfiguration {
    private String secretKey;
    private long ttlMillis;
    private String tokenName;


}
