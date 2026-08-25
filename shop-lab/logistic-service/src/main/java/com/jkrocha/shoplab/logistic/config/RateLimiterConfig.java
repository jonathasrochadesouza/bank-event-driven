package com.jkrocha.shoplab.logistic.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a per-instance Guava {@link RateLimiter}. This caps processing to N
 * permits/second; excess events wait in the topic as consumer lag rather than
 * being dropped. Note the limit is per instance, so N replicas yield ~N times
 * the aggregate throughput.
 */
@Configuration
@SuppressWarnings("UnstableApiUsage")
public class RateLimiterConfig {

    @Bean
    public RateLimiter processingRateLimiter(
            @Value("${app.rate-limit.permits-per-second}") double permitsPerSecond) {
        return RateLimiter.create(permitsPerSecond);
    }
}
