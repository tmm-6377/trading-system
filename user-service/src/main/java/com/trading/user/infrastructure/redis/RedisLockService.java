package com.trading.user.infrastructure.redis;

import com.trading.common.exception.LockAcquireException;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisLockService {
    private final StringRedisTemplate redisTemplate;
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
        "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
        Long.class
    );

    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        String token = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, token, Duration.ofSeconds(30));
        if (!Boolean.TRUE.equals(acquired)) {
            throw new LockAcquireException("重复下单，请稍后重试");
        }
        try {
            return supplier.get();
        } finally {
            redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), token);
        }
    }
}
