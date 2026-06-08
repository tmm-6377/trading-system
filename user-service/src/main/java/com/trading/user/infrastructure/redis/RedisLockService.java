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

/**
 * Redis 分布式锁服务
 *
 * <p>基于 Redis SET NX EX 命令实现的分布式锁，用于防止并发重复操作（如重复下单）。</p>
 *
 * <p>实现原理：</p>
 * <ol>
 *   <li>加锁：使用 SET key value NX EX 30 原子命令，NX确保只有一个线程成功</li>
 *   <li>持有者标识：value 使用 UUID，确保只有持有锁的线程才能释放</li>
 *   <li>自动超时：锁的过期时间为30秒，防止死锁</li>
 *   <li>释放锁：使用 Lua 脚本原子地检查并删除，防止误删其他线程的锁</li>
 * </ol>
 *
 * <p>使用场景：防止同一用户对同一商品重复下单</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class RedisLockService {

    /** Redis 字符串操作模板 */
    private final StringRedisTemplate redisTemplate;

    /**
     * 释放锁的 Lua 脚本
     * 原子性地检查锁的持有者并删除：如果key的值等于token才删除，防止误删
     */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
        "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
        Long.class
    );

    /**
     * 在分布式锁保护下执行业务逻辑
     *
     * <p>加锁流程：</p>
     * <ol>
     *   <li>尝试获取 Redis 锁（SET NX EX 30秒）</li>
     *   <li>获取失败则抛出 LockAcquireException</li>
     *   <li>获取成功则执行 supplier 业务逻辑</li>
     *   <li>finally 块中释放锁（Lua脚本保证原子性）</li>
     * </ol>
     *
     * @param lockKey  锁的 Redis Key，建议格式为 "业务:参数1:参数2"
     * @param supplier 需要在锁保护下执行的业务逻辑
     * @param <T>      返回值类型
     * @return 业务逻辑的执行结果
     * @throws LockAcquireException 如果获取锁失败（说明有并发操作）
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        // 生成唯一 token，确保只有持有锁的线程才能释放
        String token = UUID.randomUUID().toString();
        // 尝试获取分布式锁（SET NX EX 30秒）
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, token, Duration.ofSeconds(30));
        if (!Boolean.TRUE.equals(acquired)) {
            throw new LockAcquireException("重复下单，请稍后重试");
        }
        try {
            // 在锁保护下执行业务逻辑
            return supplier.get();
        } finally {
            // 使用 Lua 脚本原子释放锁，防止误删其他线程的锁
            redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), token);
        }
    }
}
