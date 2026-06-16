package com.bebasit.brunolicenseserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private long capacity = 10;

    private long refillTokens = 10;

    private long refillDurationMinutes = 1;
}
