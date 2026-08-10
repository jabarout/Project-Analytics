package com.projectanalytics.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis infrastructure configuration.
 *
 * <p>Redis is used only as a cache (not a system of record). Domain caching
 * strategies are introduced with analytics and dashboard milestones.
 */
@Configuration
@ConditionalOnProperty(name = "projectanalytics.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfiguration {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
