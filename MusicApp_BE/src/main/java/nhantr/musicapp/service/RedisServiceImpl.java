package nhantr.musicapp.service;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisServiceImpl implements RedisService {

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";

    private final StringRedisTemplate redisTemplate;

    public RedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklistToken(String token, long ttlInMs) {
        try {
            redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + token, "1", Duration.ofMillis(Math.max(ttlInMs, 1L)));
        } catch (DataAccessException ex) {
            log.warn("Failed to write blacklisted token to Redis: {}", ex);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
        } catch (DataAccessException ex) {
            log.warn("Failed to read blacklisted token from Redis: {}", ex);
            return false;
        }
    }

    @Override
    public void setValue(String key, String value, long ttlInSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(Math.max(ttlInSeconds, 1L)));
        } catch (DataAccessException ex) {
            log.warn("Failed to set Redis key '{}': {}", key, ex);
        }
    }

    @Override
    public String getValue(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (DataAccessException ex) {
            log.warn("Failed to read Redis key '{}': {}", key, ex);
            return null;
        }
    }

    @Override
    public void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException ex) {
            log.warn("Failed to delete Redis key '{}': {}", key, ex);
        }
    }
}
