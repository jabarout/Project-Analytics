package com.projectanalytics.synchronization.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Invalidates cached dashboard/KPI entries after synchronization.
 * No-op when Redis is disabled.
 */
@Service
public class CacheInvalidationService {

    private static final Logger log = LoggerFactory.getLogger(CacheInvalidationService.class);

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    public CacheInvalidationService(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.redisTemplateProvider = redisTemplateProvider;
    }

    public void invalidateWorkspaceCaches(UUID workspaceId) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            log.debug("Redis unavailable; skipping cache invalidation for workspace {}", workspaceId);
            return;
        }
        try {
            // Domain cache keys will be namespaced in later milestones; clear known prefixes for now.
            var keys = redisTemplate.keys("pa:cache:workspace:" + workspaceId + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            log.info("Cache invalidated for workspace {}", workspaceId);
        } catch (RuntimeException exception) {
            log.warn("Cache invalidation failed for workspace {}: {}", workspaceId, exception.getMessage());
        }
    }
}
