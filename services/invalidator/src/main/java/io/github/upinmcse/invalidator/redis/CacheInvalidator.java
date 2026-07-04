package io.github.upinmcse.invalidator.redis;

import io.github.upinmcse.common.cache.CacheKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


@Component
public class CacheInvalidator {

    private static final Logger LOG = LoggerFactory.getLogger(CacheInvalidator.class);

    private final StringRedisTemplate redis;

    public CacheInvalidator(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void invalidate(long itemId) {
        String key = CacheKeys.itemById(itemId);
        Boolean deleted = redis.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            LOG.debug("invalidated cache key={}", key);
        } else {
            LOG.trace("cache key already absent (no-op DEL): {}", key);
        }
    }
}
